package searchengine.services;

import searchengine.dto.searchModel.ResultSearch;
import searchengine.model.Site;

import java.util.List;

public interface IndexingService {

    void startIndexing();

    void stopIndexing();

    boolean statusIndexing();

    List<Site> getListSiteIndexing();

    void indexPage(String url);

    ResultSearch searchPage(String query, String site);
}
