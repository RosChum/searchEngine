package searchengine.services;

import searchengine.dto.searchModel.ResultSearch;
import searchengine.model.Site;

public interface IndexingService {

    void startIndexing();

    void stopIndexing();

    boolean statusIndexing();

    void indexPage(String url, Site site);

    ResultSearch searchPage(String query, String site, int limit, int offset);

    Site getSiteFromDB(String url);
}
