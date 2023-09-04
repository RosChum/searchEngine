package searchengine.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IndexingServiceImplTest {

    @MockBean
    private SiteRepository siteRepository;
    @MockBean
    private PageRepository pageRepository;
    @MockBean
    private LemmaRepository lemmaRepository;
    @MockBean
    private IndexRepository indexSearchRepository;

    private String url;
    private Site site;

    private IndexingServiceImpl indexingService;

    @Autowired
    public IndexingServiceImplTest(IndexingServiceImpl indexingService) {

        this.indexingService = indexingService;

    }

    @Test
    void startIndexing() {
        indexingService.startIndexing();
        Mockito.verify(siteRepository, Mockito.times(6)).save(Mockito.any(Site.class));


    }

    @BeforeEach
    void setUp() {
        url = "https://automationintesting.online/";
        site = new Site();
        site.setUrl("https://automationintesting.online/");

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void stopIndexing() {

        indexingService.stopIndexing();

    }



}