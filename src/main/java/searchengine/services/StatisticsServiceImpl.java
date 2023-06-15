package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexSearchRepository;


    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(siteRepository.findAll().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<searchengine.model.Site> sitesList = siteRepository.findAll();
        for (int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            int pagesCount = pageRepository.findBySite(site).size();
            int lemmasCount = lemmaRepository.findBySite(site).size();

            total.setPages(total.getPages() + pagesCount);
            total.setLemmas(total.getLemmas() + lemmasCount);
            detailed.add(getDetailedStatisticsItem(site,pagesCount,lemmasCount));
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
    //TODO Check method on error statistic
    private DetailedStatisticsItem getDetailedStatisticsItem(Site site,int pagesCount, int lemmasCount ){
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setPages(pagesCount);
        item.setLemmas(lemmasCount);
        item.setStatus(site.getStatus().toString());
        item.setError(site.getLastError());
        item.setStatusTime(site.getStatusTime().getLong(ChronoField.SECOND_OF_DAY));
        return item;
    }


}
