package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.searchModel.DtoSearchPageInfo;
import searchengine.dto.searchModel.ResultSearch;
import searchengine.model.IndexingStatus;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexSearchRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.utility.LemmaСonverter;
import searchengine.utility.SiteIndexMap;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IndexingServiceImpl implements IndexingService {


    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private SitesList sitesList;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor threadPoolExecutor;
    private LemmaRepository lemmaRepository;
    private IndexSearchRepository indexSearchRepository;
    private ResultSearch resultSearch;


    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList,
                               LemmaRepository lemmaRepository, IndexSearchRepository indexSearchRepository) {
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
            siteFromAppProperties.setUrl(siteFromAppProperties.getUrl().replace("www.", ""));
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
            threadPoolExecutor.shutdownNow();
            SiteIndexMap.stop = true;
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
    public void indexPage(String url) {
        Site site = new Site();

        siteRepository.delete(siteRepository.findByUrl(url));

        searchengine.config.Site siteFromConfig = sitesList.getSites().stream()
                .filter(site1 -> site1.getUrl().contains(url.substring(url.indexOf("//"), url.lastIndexOf(".")))).findFirst().get();
        site.setName(siteFromConfig.getName());
        site.setUrl(siteFromConfig.getUrl());
        site.setStatus(IndexingStatus.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(null);

        siteRepository.save(site);

        walkAndIndexSite(site.getUrl(), siteRepository, pageRepository, site, lemmaRepository, indexSearchRepository);

    }

    @Override
    public ResultSearch searchPage(String query, String site) {
        ResultSearch resultSearch = new ResultSearch();
        List<Lemma> foundLemmaListFromQuery = new ArrayList<>();
        Set<Page> foundListPageByFirstLemma;
        LemmaСonverter lemmaСonverter = new LemmaСonverter();
        try {
            Set<String> queryLemmas = lemmaСonverter.convertTextToLemmas(query).keySet();
            for (String lemmas : queryLemmas) {
                if (site == null || site.isEmpty()) {
                    foundLemmaListFromQuery.addAll(lemmaRepository.findByLemmaOrderByFrequencyAsc(lemmas)); // находим все леммы из запроса
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

                resultSearch = searchMatches(foundListPageByFirstLemma, sortedFoundLemmaListFromQuery);

            } else {
                resultSearch.setResult(false);
                return resultSearch;
            }

        } catch (IOException e) {
            e.printStackTrace();

        }

        return resultSearch;

    }


    private void walkAndIndexSite(String urlSite, SiteRepository siteRepository, PageRepository pageRepository, Site site,
                                  LemmaRepository lemmaRepository, IndexSearchRepository indexSearchRepository) {
        SiteIndexMap.stop = false;
        SiteIndexMap siteIndexMap = new SiteIndexMap(urlSite, siteRepository, pageRepository, site, lemmaRepository, indexSearchRepository);
        forkJoinPool = new ForkJoinPool(10);
        Site site1 = forkJoinPool.invoke(siteIndexMap);
        if (site1.getStatus() != IndexingStatus.FAILED) {
            siteRepository.updateStatus(site.getName(), IndexingStatus.INDEXED, null, LocalDateTime.now());
        }
        if (site1.getStatus() != IndexingStatus.FAILED && site1.getStatus() != IndexingStatus.INDEXED && SiteIndexMap.stop) {
            siteRepository.updateStatus(site.getName(), IndexingStatus.FAILED, "Индексация остановлена пользователем", LocalDateTime.now());
        }
        forkJoinPool.shutdown();
    }

    private ResultSearch searchMatches(Set<Page> foundListPageByFirstLemma, List<Lemma> sortedFoundLemmaListFromQuery) {
        Set<String> getLemmasStringType = new HashSet<>();
        Set<Page> workingListPage = new HashSet<>(Set.copyOf(foundListPageByFirstLemma));

        getLemmasStringType.addAll(sortedFoundLemmaListFromQuery.stream().map(Lemma::getLemma).collect(Collectors.toSet()));

        for (Page page : foundListPageByFirstLemma) {
            Set<String> lemmasSetByPage = page.getIndexSearches().stream().map(f -> f.getLemma().getLemma()).collect(Collectors.toSet());
            if (!lemmasSetByPage.containsAll(getLemmasStringType)) {
                workingListPage.remove(page);
            } else {
                return getResultSearch(page, getLemmasStringType);
            }

        }

        return null;

    }

    private ResultSearch getResultSearch(Page page, Set<String> lemmasListFromQuery) {

        AtomicInteger calculatingRelevance = new AtomicInteger();
        StringBuilder snippet = new StringBuilder();

        ResultSearch resultSearch = new ResultSearch();
        List<DtoSearchPageInfo> findPage = new ArrayList<>();
        page.getIndexSearches().forEach(p -> {
            if (lemmasListFromQuery.contains(p.getLemma().getLemma())) {
                calculatingRelevance.addAndGet(p.getRank());

                Document document = Jsoup.parse(p.getPage().getContent());
                String regexSnippet = "(?<=[.!?]\\s).*\\b" + p.getLemma().getLemma() + "\\b.{0,100}[.!?]";
                Pattern pattern = Pattern.compile(regexSnippet);
                Matcher matcher = pattern.matcher(document.text().toLowerCase(Locale.ROOT));

                while (matcher.find()) {
                    snippet.append(matcher.group());
                }
                DtoSearchPageInfo dtoSearchPageInfo = new DtoSearchPageInfo();
                dtoSearchPageInfo.setRelevance(calculatingRelevance.get());
                dtoSearchPageInfo.setSite(p.getPage().getSite().getUrl());
                dtoSearchPageInfo.setSiteName(p.getPage().getSite().getName());
                dtoSearchPageInfo.setSnippet(snippet.toString());
                dtoSearchPageInfo.setTitle(document.select("title").text());
                dtoSearchPageInfo.setUri(p.getPage().getPath());
                findPage.add(dtoSearchPageInfo);

            }

        });
        resultSearch.setResult(true);
        resultSearch.setCount(findPage.size());
        resultSearch.setData(findPage.stream().sorted(Comparator.comparing(DtoSearchPageInfo::getRelevance).reversed()).collect(Collectors.toList()));

        return resultSearch;
    }


}
