package searchengine.dto.searchModel;

import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class ResultSearch {

    private boolean result;
    private int count;
    private List<DtoSearchPageInfo> data;
    private Pageable pageable;


}
