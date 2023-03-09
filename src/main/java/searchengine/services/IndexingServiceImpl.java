package searchengine.services;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class IndexingServiceImpl implements IndexingService {


    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private SitesList sitesList;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    public IndexingServiceImpl(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;

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

            threadPoolExecutor.submit(() -> walkAndIndexSite(site.getUrl(), siteRepository, pageRepository, site));


            System.out.println("FINISH!!");
            System.out.println(threadPoolExecutor.getQueue().size());
        });

        threadPoolExecutor.shutdown();
        threadPoolExecutor.getQueue().clear();


    }

    @Override
    public void stopIndexing() {
        if (threadPoolExecutor.isTerminating()) {
            threadPoolExecutor.shutdownNow();
            siteRepository.findAll().forEach(site -> {
                siteRepository.updateStatus(site.getName(),
                        IndexingStatus.FAILED, "Индексация остановлена пользователем", LocalDateTime.now());
            });
        }
    }


    private void walkAndIndexSite(String urlSite, SiteRepository siteRepository, PageRepository pageRepository, Site site) {
        SiteIndexMap siteIndexMap = new SiteIndexMap(urlSite, siteRepository, pageRepository, site);
        forkJoinPool = new ForkJoinPool();
        System.out.println("Finish walkAndIndexSite");
        Site site1 = forkJoinPool.invoke(siteIndexMap);
        if (site1.getStatus() != IndexingStatus.FAILED) {
            siteRepository.updateStatus(site.getName(), IndexingStatus.INDEXED, null,LocalDateTime.now());
        }
        forkJoinPool.shutdown();


    }



}
