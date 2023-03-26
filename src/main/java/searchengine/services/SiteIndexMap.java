package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.LemmaСonverter;
import searchengine.model.IndexingStatus;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexSearchRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private LemmaСonverter lemmaСonverter;


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

            Document document = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; " +
                            "en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").timeout(500)
                    .referrer("http://www.google.com").get();

            Elements elements = document.select("a[href]");

            elements.forEach(element -> {
                childUrl = element.absUrl("href");

                if (checkURL(childUrl, site)) {

                    Connection.Response responseCode = document.connection().response();

                    addPageInDb(childUrl, responseCode.statusCode(), document.toString(), site);

                    if (responseCode.statusCode() == 200) {
                        addLemmaEndIndexDB(document.text(), site);
                    }

                    siteRepository.findByUrl(site.getUrl()).setStatusTime(LocalDateTime.now());

                    System.out.println(Thread.currentThread().getId() + " ->> " + childUrl);
                    SiteIndexMap siteIndexMap = new SiteIndexMap(childUrl, siteRepository, pageRepository, site,
                            lemmaRepository, indexSearchRepository);

                    task.add(siteIndexMap);

                }

            });


        } catch (HttpStatusException ex) {
            ex.printStackTrace();
            addPageInDb(url, ex.getStatusCode(), ex.toString(), site);


        } catch (IOException | InterruptedException exception) {
            site.setStatus(IndexingStatus.FAILED);
            site.setLastError(exception.toString());
            site.setStatusTime(LocalDateTime.now());
            siteRepository.updateStatus(site.getName(), IndexingStatus.FAILED, exception.toString(), LocalDateTime.now());
            return site;
        }
        invokeAll(task);
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

    private void addLemmaEndIndexDB(String text, Site site) {

        lemmaСonverter = new LemmaСonverter();

        try {
            HashMap<String, Integer> lemmas = lemmaСonverter.convertTextToLemmas(text);
            lemmas.forEach((keyLemma, value) -> {

                lemma = new Lemma();
                lemma.setLemma(keyLemma);
                lemma.setSite(site);
                lemma.setFrequency(value);

                if (lemmaRepository.findByLemmaAndSite(keyLemma, site) != null) {
                    lemmaRepository.updateFrequency(keyLemma, lemmaRepository.findByLemma(keyLemma).getFrequency() + 1, site);
                } else {
                    lemmaRepository.save(lemma);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



