package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.searchModel.DtoSearchPageInfo;
import searchengine.dto.searchModel.ResultSearch;
import searchengine.model.*;
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

            }

        }
        return getResultSearch(workingListPage, getLemmasStringType);

    }

    private ResultSearch getResultSearch(Set<Page> pageSet, Set<String> lemmasListFromQuery) {

        ResultSearch resultSearch = new ResultSearch();
        Set<DtoSearchPageInfo> findPage = new HashSet<>();

        for (Page page : pageSet) {

            List<IndexSearch> indexSearches = page.getIndexSearches();

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
        getRelativeRelevance(findPage);

        findPage.forEach(f -> System.out.println(f.getSite() + "\n" + f.getUri() + "\n" + f.getRelevance()));

        resultSearch.setResult(true);
        resultSearch.setCount(findPage.size());
        resultSearch.setData(findPage.stream().sorted(Comparator.comparing(DtoSearchPageInfo::getRelevance).reversed()).collect(Collectors.toList()));

        return resultSearch;
    }

    private double getAbsoluteRelevance(List<IndexSearch> indexSearches, Set<String> lemmasListFromQuery) {

        AtomicInteger absoluteRelevance = new AtomicInteger();

        indexSearches.forEach(i -> {

            if (lemmasListFromQuery.contains(i.getLemma().getLemma()))
                absoluteRelevance.addAndGet(i.getRank());

        });

        return absoluteRelevance.doubleValue();
    }

    private String getSnippet(List<IndexSearch> indexSearches, Set<String> lemmasListFromQuery) {

        StringBuilder snippet = new StringBuilder();
        indexSearches.forEach(p -> {

            if (lemmasListFromQuery.contains(p.getLemma().getLemma())) {
                Document document = Jsoup.parse(p.getPage().getContent());
//                String regexSnippet = "(?<=[.!?]\\s).{0,100}\\b" + p.getLemma().getLemma() + "\\b.{0,100}[.!?]";
                String regexSnippet = ".{0,30}\\b" + p.getLemma().getLemma() + "\\b.{0,30}";

                Pattern pattern = Pattern.compile(regexSnippet);
                Matcher matcher = pattern.matcher(document.text().toLowerCase(Locale.ROOT));

                while (matcher.find()) {
                    String substring = matcher.group();
                    snippet.append(substring.replaceAll(p.getLemma().getLemma(), "<b>" + p.getLemma().getLemma() + "</b>"));
                }
            }
        });

        return snippet.toString();
    }

    private String getTitle(Document document) {
        return document.select("title").text() + "\n" + document.select("h2").text();
    }

    private void getRelativeRelevance(Set<DtoSearchPageInfo> findPage) {

        List<DtoSearchPageInfo> workList = findPage.stream().sorted(Comparator.comparing(DtoSearchPageInfo::getRelevance)).collect(Collectors.toList());

        double tmp;

        for (DtoSearchPageInfo dtoSearchPageInfo : workList) {

            tmp = dtoSearchPageInfo.getRelevance() / workList.get(workList.size() - 1).getRelevance();
            dtoSearchPageInfo.setRelevance(Math.floor(tmp * 100) / 100);
        }


    }


}
