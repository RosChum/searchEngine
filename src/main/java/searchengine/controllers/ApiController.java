package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.dto.StatusRequest;
import searchengine.dto.searchModel.ResultSearch;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
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
        String urlAddress = url.replace("www.", "");
        StatusRequest statusRequest = new StatusRequest();
        if (indexingService.getListSiteIndexing().stream().anyMatch(s -> s.getUrl().contains(urlAddress))) {
            statusRequest.setResult(true);
            indexingService.indexPage(urlAddress);
            return ResponseEntity.ok(statusRequest);
        } else {
            statusRequest.setResult(false);
            statusRequest.setError("Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");

            return ResponseEntity.ok(statusRequest);
        }


    }

    @GetMapping(value = "/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String query, @RequestParam(required = false, name = ("site")) String site) {
        StatusRequest statusRequest = new StatusRequest();

        if (query.isEmpty() || query.equals("\s")) {
            statusRequest.setResult(false);
            statusRequest.setError("Задан пустой поисковый запрос");
            return ResponseEntity.ok(statusRequest);
        }

        ResultSearch resultSearch = indexingService.searchPage(query, site);
        if (!resultSearch.isResult()) {
            statusRequest.setResult(false);
            statusRequest.setError("Указанная страница не найдена");
            return ResponseEntity.status(404).body(statusRequest);
        }

        return ResponseEntity.ok(resultSearch);
    }

}
