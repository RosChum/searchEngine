package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.searchModel.DtoSearchPageInfo;
import searchengine.dto.searchModel.ResultSearch;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.utility.FindMatchesSnippets;
import searchengine.utility.LemmaСonverter;
import searchengine.utility.SiteIndexing;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IndexingServiceImpl implements IndexingService {


    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private SitesList sitesList;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor threadPoolExecutor;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexSearchRepository;

    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList,
                               LemmaRepository lemmaRepository, IndexRepository indexSearchRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
        this.lemmaRepository = lemmaRepository;
        this.indexSearchRepository = indexSearchRepository;

    }

    @Override
    public void startIndexing() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(sitesList.getSites().size());
        sitesList.getSites().forEach(siteFromAppProperties -> {
            siteFromAppProperties.setUrl(bringingWebsiteAddressToSingleFormat(siteFromAppProperties.getUrl()));
            if (siteRepository.existsByUrl(siteFromAppProperties.getUrl())) {
                siteRepository.delete(siteRepository.findByUrl(siteFromAppProperties.getUrl()));
            }
            Site site = new Site();
            site.setName(siteFromAppProperties.getName());
            site.setUrl(siteFromAppProperties.getUrl());
            site.setStatus(IndexingStatus.INDEXING);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError(null);
            siteRepository.save(site);

            threadPoolExecutor.submit(() -> walkAndIndexSite(site.getUrl(), siteRepository,
                    pageRepository, site, lemmaRepository, indexSearchRepository));

        });

        threadPoolExecutor.shutdown();
        threadPoolExecutor.getQueue().clear();

    }

    @Override
    public void stopIndexing() {
        if (statusIndexing()) {
            threadPoolExecutor.shutdown();
            SiteIndexing.stopParsing = true;
            ForkJoinPool.commonPool().shutdownNow();

        }

    }

    @Override
    public boolean statusIndexing() {
        if (threadPoolExecutor == null || threadPoolExecutor.getActiveCount() == 0) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public List<Site> getListSiteIndexing() {
        return new ArrayList<>(siteRepository.findAll());
    }

    @Override
    public void indexPage(String url, Site site) {
        String urlSite = bringingWebsiteAddressToSingleFormat(url);
        Page page;
        String regexForPagePath = "(?<=[^/])/{1}(?=[^/]).*";
        Pattern pattern = Pattern.compile(regexForPagePath);
        Matcher matcher = pattern.matcher(url);

        while (matcher.find()) {
            page = pageRepository.findByPathAndSite(matcher.group(), site);

            if (pageRepository.existsByPathAndSite(matcher.group(), site)) {
                pageRepository.delete(page);
            }

        }

        walkAndIndexSite(urlSite, siteRepository, pageRepository, site, lemmaRepository, indexSearchRepository);

    }

    @Override
    public ResultSearch searchPage(String query, String site, int limit, int offset) {
        ResultSearch resultSearch = new ResultSearch();
        List<Lemma> foundLemmaListFromQuery = new ArrayList<>();
        Set<Page> foundListPageByFirstLemma;
        LemmaСonverter lemmaСonverter = new LemmaСonverter();

            Set<String> queryLemmas = lemmaСonverter.convertTextToLemmas(query).keySet();

            for (String lemmas : queryLemmas) {

                if (site == null || site.isEmpty()) {

                    foundLemmaListFromQuery.addAll(lemmaRepository.findByLemma(lemmas)); // находим все леммы из запроса
                } else {
                    Site site1 = siteRepository.findByUrl(site);
                    foundLemmaListFromQuery.addAll(lemmaRepository.findLemmasByLemmaAndSite(lemmas, site1));// находим все леммы из запроса на сайте
                }
            }

            int countPage = pageRepository.findAll().size();
            List<Lemma> sortedFoundLemmaListFromQuery = foundLemmaListFromQuery.stream().filter(lemma -> lemma.getFrequency() < countPage * 0.37)
                    .sorted(Comparator.comparing(Lemma::getFrequency)).toList(); //находим все леммы из запроса, убираем часто встречающиеся, сортируем по возрастанию

            if (sortedFoundLemmaListFromQuery.size() > 0) {
                foundListPageByFirstLemma = sortedFoundLemmaListFromQuery.stream().flatMap(s -> s.getIndexSearches().stream()
                        .filter(f -> f.getLemma().getLemma().equals(sortedFoundLemmaListFromQuery.get(0).getLemma()))
                        .map(f -> f.getPage())).collect(Collectors.toSet()); //находим все страницы по первой лемме, получаем страницы

                resultSearch = searchMatches(foundListPageByFirstLemma, sortedFoundLemmaListFromQuery, limit, offset);

            } else {
                resultSearch.setResult(false);
                return resultSearch;
            }


        return resultSearch;

    }


    private void walkAndIndexSite(String urlSite, SiteRepository siteRepository, PageRepository pageRepository, Site site,
                                  LemmaRepository lemmaRepository, IndexRepository indexSearchRepository) {
        SiteIndexing.stopParsing = false;
        SiteIndexing siteIndexMap = new SiteIndexing(urlSite, siteRepository, pageRepository, site, lemmaRepository, indexSearchRepository);
        forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        Site site1 = forkJoinPool.invoke(siteIndexMap);
        if (site1.getStatus() != IndexingStatus.FAILED) {
            siteRepository.updateStatus(site.getName(), IndexingStatus.INDEXED, null, LocalDateTime.now());
        }
        if (site1.getStatus() != IndexingStatus.FAILED && site1.getStatus() != IndexingStatus.INDEXED && SiteIndexing.stopParsing) {
            siteRepository.updateStatus(site.getName(), IndexingStatus.FAILED, "Индексация остановлена пользователем", LocalDateTime.now());
        }
        forkJoinPool.shutdown();
    }

    private ResultSearch searchMatches(Set<Page> foundListPageByFirstLemma, List<Lemma> sortedFoundLemmaListFromQuery, int limit, int offset) {
        Set<String> getLemmasStringType = new HashSet<>();
        Set<Page> workingListPage = new HashSet<>(Set.copyOf(foundListPageByFirstLemma));
        getLemmasStringType.addAll(sortedFoundLemmaListFromQuery.stream().map(Lemma::getLemma).collect(Collectors.toSet()));
        for (Page page : foundListPageByFirstLemma) {
            Set<String> lemmasSetByPage = page.getIndexSearches().stream().map(f -> f.getLemma().getLemma()).collect(Collectors.toSet());
            if (!lemmasSetByPage.containsAll(getLemmasStringType)) {
                workingListPage.remove(page);

            }

        }
        return getResultSearch(workingListPage, getLemmasStringType, limit, offset);

    }

    private ResultSearch getResultSearch(Set<Page> pageSet, Set<String> lemmasListFromQuery, int limit, int offset) {

        ResultSearch resultSearch = new ResultSearch();
        Set<DtoSearchPageInfo> findPage = new HashSet<>();

        for (Page page : pageSet) {

            Set<Index> indexSearches = page.getIndexSearches().stream().collect(Collectors.toSet());

            Document document = Jsoup.parse(page.getContent());

            DtoSearchPageInfo dtoSearchPageInfo = new DtoSearchPageInfo();
            dtoSearchPageInfo.setRelevance(getAbsoluteRelevance(indexSearches, lemmasListFromQuery));
            dtoSearchPageInfo.setSite(page.getSite().getUrl());
            dtoSearchPageInfo.setSiteName(page.getSite().getName());
            dtoSearchPageInfo.setSnippet(getSnippet(indexSearches, lemmasListFromQuery));
            dtoSearchPageInfo.setTitle(getTitle(document));
            dtoSearchPageInfo.setUri(page.getPath());

            findPage.add(dtoSearchPageInfo);

        }

        setRelativeRelevance(findPage);
        resultSearch.setCount(findPage.size());
        resultSearch.setData(findPage.stream().sorted(Comparator.comparing(DtoSearchPageInfo::getRelevance).reversed()).skip(offset).limit(limit).collect(Collectors.toList()));
        resultSearch.setResult(resultSearch.getData().size() > 0);
        return resultSearch;
    }

    private double getAbsoluteRelevance(Set<Index> indexSearches, Set<String> lemmasListFromQuery) {

        AtomicInteger absoluteRelevance = new AtomicInteger();

        indexSearches.forEach(i -> {

            if (lemmasListFromQuery.contains(i.getLemma().getLemma()))
                absoluteRelevance.addAndGet(i.getRank());

        });

        return absoluteRelevance.doubleValue();
    }

    private String getSnippet(Set<Index> indexSearches, Set<String> lemmasListFromQuery) {
        ThreadPoolExecutor threadPoolExecutorForSnippet = (ThreadPoolExecutor) Executors.newFixedThreadPool(12);
        StringBuilder snippet = new StringBuilder();
        indexSearches.forEach(p -> {

            if (lemmasListFromQuery.contains(p.getLemma().getLemma())) {

                try {
                    snippet.append(threadPoolExecutorForSnippet.submit(() ->
                            new FindMatchesSnippets(p.getLemma().getLemma(), p.getPage().getContent()).call()).get());
                } catch (InterruptedException | ExecutionException e) {
                   log.error(e.getMessage(), e);
                }
            }

        });
        threadPoolExecutorForSnippet.shutdown();
        return snippet.toString();
    }

    private String getTitle(Document document) {
        return document.select("title").text() + "\n" + document.select("h2").text();
    }

    private void setRelativeRelevance(Set<DtoSearchPageInfo> findPage) {

        List<DtoSearchPageInfo> workList = findPage.stream().sorted(Comparator.comparing(DtoSearchPageInfo::getRelevance)).collect(Collectors.toList());

        double tmp;

        for (DtoSearchPageInfo dtoSearchPageInfo : workList) {

            tmp = dtoSearchPageInfo.getRelevance() / workList.get(workList.size() - 1).getRelevance();
            dtoSearchPageInfo.setRelevance(Math.floor(tmp * 100) / 100);
        }


    }

    @Override
    public Site getSiteFromDB(String url) {

        Site site = new Site();
        String urlSite = bringingWebsiteAddressToSingleFormat(url);
        String regexSite = "h.*//[^/]*";
        Pattern pattern = Pattern.compile(regexSite);
        Matcher matcher = pattern.matcher(urlSite);
        while (matcher.find()) {
            site = siteRepository.findByUrl(matcher.group());
        }
        return site;
    }

    private String bringingWebsiteAddressToSingleFormat(String url) {
        return url.replace("www.", "");
    }

}
