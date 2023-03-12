package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.model.IndexingStatus;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public SiteIndexMap(String url, SiteRepository siteRepository, PageRepository pageRepository, Site site) {
        this.url = url;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.site = site;
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

                    siteRepository.findByUrl(site.getUrl()).setStatusTime(LocalDateTime.now());

                    System.out.println(Thread.currentThread().getId() + " ->> " + childUrl);
                    SiteIndexMap siteIndexMap = new SiteIndexMap(childUrl, siteRepository, pageRepository, site);
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


}



