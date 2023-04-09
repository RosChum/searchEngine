package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.LemmaСonverter;
import searchengine.model.*;
import searchengine.repository.IndexSearchRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveTask;

public class SiteIndexMap extends RecursiveTask<Site> {

    private String url;
    private String childUrl;
    private Page page;
    private Site site;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private List<SiteIndexMap> task = new ArrayList<>();
    private LemmaRepository lemmaRepository;
    private IndexSearchRepository indexSearchRepository;
    private Lemma lemma;
    private LemmaСonverter lemmaСonverter = new LemmaСonverter();
    private IndexSearch indexSearch;
    public static boolean stop;
    private HashMap<String, Integer> lemmas;


    public SiteIndexMap(String url, SiteRepository siteRepository, PageRepository pageRepository, Site site, LemmaRepository lemmaRepository, IndexSearchRepository indexSearchRepository) {
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.site = site;
        this.lemmaRepository = lemmaRepository;
        this.indexSearchRepository = indexSearchRepository;
    }

    @Override
    protected Site compute() {

        try {

            Thread.sleep(250);

            Document document = Jsoup.connect(url).get();

            Elements elements = document.select("a[href]");

            elements.forEach(element -> {
                childUrl = element.absUrl("href");

                if (checkURL(childUrl, site)) {

                    Connection.Response responseCode = document.connection().response();

                    addPageInDb(childUrl, responseCode.statusCode(), document.toString(), site);

                    siteRepository.findByUrl(site.getUrl()).setStatusTime(LocalDateTime.now());

                    if (responseCode.statusCode() == 200)
                        addLemmaDB(document.text(), site);

                    SiteIndexMap siteIndexMap = new SiteIndexMap(childUrl, siteRepository, pageRepository, site,
                            lemmaRepository, indexSearchRepository);

                    if (stop) {
                        task.clear();
                        ForkJoinWorkerThread.currentThread().interrupt();
                    } else {
                        siteIndexMap.fork();
                        task.add(siteIndexMap);
                    }
                }

            });


        } catch (HttpStatusException ex) {
            ex.printStackTrace();
            addPageInDb(url, ex.getStatusCode(), ex.toString(), site);

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
            site.setStatus(IndexingStatus.FAILED);
            site.setLastError(exception.toString());
            site.setStatusTime(LocalDateTime.now());
            siteRepository.updateStatus(site.getName(), IndexingStatus.FAILED, exception.toString(), LocalDateTime.now());
            return site;

        }

        for (SiteIndexMap site1 : task) {
            site1.join();
        }

        return site;
    }


    private synchronized void addPageInDb(String path, int codeResponse, String content, Site site) {
        page = new Page();
        page.setPath(path.substring(site.getUrl().length()));
        page.setCodeResponse(codeResponse);
        page.setContent(content);
        page.setSite(site);
        pageRepository.save(page);

    }

    private boolean checkURL(String url, Site site) {

        return url.startsWith(this.url) && url.endsWith("/") &&
                !pageRepository.existsByPath(url.substring(site.getUrl().length()));
    }

    private synchronized void addLemmaDB(String text, Site site) {

        if (!stop) {
            try {
                lemmas = lemmaСonverter.convertTextToLemmas(text);
                lemmas.forEach((keyLemma, value) -> {
                    lemma = new Lemma();
                    lemma.setLemma(keyLemma);
                    lemma.setSite(site);
                    lemma.setFrequency(1);

                    if (lemmaRepository.existsLemmaByLemmaAndSite(keyLemma, site)) {
                        lemmaRepository.updateFrequency(keyLemma, site);

                    } else {
                        lemmaRepository.save(lemma);
                        addIndexDB(lemma, page, value);
                    }

                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            lemmas.clear();
            ForkJoinWorkerThread.currentThread().interrupt();
        }

    }

    private synchronized void addIndexDB(Lemma lemma, Page page, int value) {
        indexSearch = new IndexSearch();
        indexSearch.setLemma(lemma);
        indexSearch.setPage(page);
        indexSearch.setRank(value);
        indexSearchRepository.save(indexSearch);
    }

}



