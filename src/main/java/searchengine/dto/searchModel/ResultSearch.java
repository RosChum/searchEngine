package searchengine.dto.searchModel;

import lombok.Data;

import java.util.List;

@Data
public class ResultSearch {

    private boolean result;
    private int count;
    private List<DtoSearchPageInfo> data;

}
