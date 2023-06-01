package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;
import searchengine.config.SitesList;
import searchengine.dto.StatusRequest;
import searchengine.dto.searchModel.ResultSearch;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private SitesList sitesList;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {

        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping(value = "/startIndexing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusRequest> startIndexing() {
        StatusRequest statusRequest = new StatusRequest();
        if (indexingService.statusIndexing()) {
            statusRequest.setResult(false);
            statusRequest.setError("Индексация уже запущена");
            return ResponseEntity.ok(statusRequest);
        } else {
            indexingService.startIndexing();
            statusRequest.setResult(true);
            return ResponseEntity.ok(statusRequest);
        }

    }

    @GetMapping(value = "/stopIndexing", produces = MediaType.APPLICATION_JSON_VALUE)
    public StatusRequest stopIndexing() {
        StatusRequest statusRequest = new StatusRequest();
        if (indexingService.statusIndexing()) {
            statusRequest.setResult(true);
            indexingService.stopIndexing();
            return statusRequest;
        } else {
            statusRequest.setResult(false);
            statusRequest.setError("Индексация не запущена");
            return statusRequest;
        }
    }

    @PostMapping(value = "/indexPage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StatusRequest> indexPage(@RequestParam String url) {
        StatusRequest statusRequest = new StatusRequest();
        if (indexingService.getSiteFromDB(url) != null) {
            indexingService.indexPage(url, indexingService.getSiteFromDB(url));
            statusRequest.setResult(true);
        } else {
            statusRequest.setResult(false);
            statusRequest.setError("Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
        }

        return ResponseEntity.ok(statusRequest);

    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String query, @RequestParam(name = "offset") Integer offset,
                                    @RequestParam(name = "limit") Integer limit, @RequestParam(required = false, name = ("site")) String site) {

            StatusRequest statusRequest = new StatusRequest();

            if (query.isEmpty() || query.equals("\s")) {
                statusRequest.setResult(false);
                statusRequest.setError("Задан пустой поисковый запрос");
                return ResponseEntity.ok(statusRequest);
            }

            ResultSearch resultSearch = indexingService.searchPage(query, site, limit, offset);
            if (!resultSearch.isResult()) {
                statusRequest.setResult(false);
                statusRequest.setError("По указанному запросы страницы не найдены");
                return ResponseEntity.ok(statusRequest);
            }

        return ResponseEntity.ok(resultSearch);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<StatusRequest> exception(Exception exception){
//        StatusRequest statusRequest = new StatusRequest();
//        statusRequest.setResult(false);
//        statusRequest.setError("Указанная страница не найдена");
//        log.error(Arrays.toString(exception.getStackTrace()));
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(statusRequest);
//    }

}
