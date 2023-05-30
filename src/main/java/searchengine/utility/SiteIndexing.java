package searchengine.utility;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
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

@Slf4j
public class SiteIndexing extends RecursiveTask<Site> {

    private String url;
    private String childUrl;
    private Page page;
    private Site site;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private List<SiteIndexing> task = new ArrayList<>();
    private LemmaRepository lemmaRepository;
    private IndexRepository indexSearchRepository;
    private Lemma lemma;
    private LemmaСonverter lemmaСonverter = new LemmaСonverter();
    private Index indexSearch;
    public static boolean stop;
    private HashMap<String, Integer> lemmas;


    public SiteIndexing(String url, SiteRepository siteRepository, PageRepository pageRepository, Site site,
                        LemmaRepository lemmaRepository, IndexRepository indexSearchRepository) {
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

            Document document = Jsoup.connect(url).ignoreHttpErrors(false).get();
            Connection.Response responseCode = document.connection().response();
            if (responseCode.statusCode() == 400 || responseCode.statusCode() ==  401|| responseCode.statusCode() ==  403
                    || responseCode.statusCode() ==  404||responseCode.statusCode() ==  405 || responseCode.statusCode() == 500){
                throw new IOException();
            }

            Elements elements = document.select("a[href]");

            elements.forEach(element -> {
                childUrl = element.absUrl("href");

                if (checkURL(childUrl, site)) {

                    addPageInDb(childUrl, responseCode.statusCode(), document.toString(), site);

                    synchronized (SiteIndexing.class) {
                        siteRepository.updateStatusTime(site.getName(), LocalDateTime.now());
                    }

                    if (responseCode.statusCode() == 200) {
                        addLemmaDB(document.text(), site);
                    }


                    SiteIndexing siteIndexMap = new SiteIndexing(childUrl, siteRepository, pageRepository, site,
                            lemmaRepository, indexSearchRepository);

                    if (stop) {
                        task.clear();
                        return;

//                        ForkJoinWorkerThread.currentThread().interrupt();
                    } else {
                        siteIndexMap.fork();
                        task.add(siteIndexMap);
                    }
                }

            });


        } catch (HttpStatusException ex) {
            log.error("HTTP status page - " + ex.toString());
            addPageInDb(url, ex.getStatusCode(), ex.getMessage(), site);

        } catch (IOException | InterruptedException exception) {
            log.error("Site parsing error - " + exception.toString());
            site.setStatus(IndexingStatus.FAILED);
            site.setLastError("Ошибка индексации: сайт не доступен");
            site.setStatusTime(LocalDateTime.now());
            siteRepository.updateStatus(site.getName(), IndexingStatus.FAILED, "Ошибка индексации: сайт не доступен", LocalDateTime.now());
            return site;

        }

        for (SiteIndexing site1 : task) {
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
        return url.startsWith(this.url) && (url.endsWith("/") || url.endsWith("html")) &&
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
                        synchronized (LemmaRepository.class) {
                            lemmaRepository.updateFrequency(keyLemma, site);
                        }
                        lemma = lemmaRepository.findByLemmaAndSite(keyLemma, site);
                        addIndexDB(lemma, page, value);

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
        indexSearch = new Index();
        indexSearch.setLemma(lemma);
        indexSearch.setPage(page);
        indexSearch.setRank(value);
        indexSearchRepository.save(indexSearch);
    }


}



