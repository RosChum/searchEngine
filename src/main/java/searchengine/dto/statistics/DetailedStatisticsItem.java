package searchengine.dto.statistics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class DetailedStatisticsItem {
    private String url;
    private String name;
    private String status;
    private long statusTime;
    //TODO Убрать вывод пользователю null, но так не убирает, а ставит значение undefined, как и изменение конфигурации :(
//    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String error;
    private int pages;
    private int lemmas;
}
