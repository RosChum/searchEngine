package searchengine.dto.searchModel;

import lombok.Data;

@Data
public class DtoSearchPageInfo  {

    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    double relevance;


}
