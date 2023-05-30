package searchengine.services;

import org.springframework.data.domain.Pageable;
import searchengine.dto.searchModel.ResultSearch;
import searchengine.model.Site;

import java.util.List;

public interface IndexingService {

    void startIndexing();

    void stopIndexing();

    boolean statusIndexing();

    List<Site> getListSiteIndexing();

    void indexPage(String url, Site site);

    ResultSearch searchPage(String query, String site, int limit, int offset);

    Site getSiteFromDB(String url);
}
