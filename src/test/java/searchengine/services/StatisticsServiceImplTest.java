package searchengine.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StatisticsServiceImplTest {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexSearchRepository;

    private StatisticsServiceImpl statisticsService;

    @Autowired
    public StatisticsServiceImplTest(StatisticsServiceImpl statisticsService) {
        this.statisticsService = statisticsService;

    }

    @BeforeEach
    void setUp() {


    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void statistic() {
        assertNotNull(statisticsService.getStatistics());
        System.out.println(statisticsService.getStatistics());

    }

}