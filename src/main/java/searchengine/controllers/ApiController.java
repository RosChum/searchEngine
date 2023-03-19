package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.dto.statistics.Indexing.Indexing;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.ArrayList;

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
    public ResponseEntity<Indexing> startIndexing() {
        Indexing indexing = new Indexing();
        if (indexingService.statusIndexing()) {
            indexing.setResult(false);
            indexing.setError("Индексация уже запущена");
            return ResponseEntity.ok(indexing);
        } else {
            indexingService.startIndexing();
            indexing.setResult(true);
            return ResponseEntity.ok(indexing);
        }

    }

    @GetMapping(value = "/stopIndexing", produces = MediaType.APPLICATION_JSON_VALUE)
    public Indexing stopIndexing() {
        Indexing indexing = new Indexing();
        if (indexingService.statusIndexing()) {
            indexing.setResult(true);
            indexingService.stopIndexing();
            return indexing;
        } else {
            indexing.setResult(false);
            indexing.setError("Индексация не запущена");
            return indexing;
        }
    }

    @PostMapping(value = "/indexPage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Indexing> indexPage(@RequestParam String url) {
        String urlAddress  = url.replace("www.", "");
        Indexing indexing = new Indexing();
        if (indexingService.getListSiteIndexing().stream().anyMatch(s -> s.getUrl().contains(urlAddress))) {
            indexing.setResult(true);
            indexingService.indexPage(urlAddress);
            return ResponseEntity.ok(indexing);
        } else {
            indexing.setResult(false);
            indexing.setError("Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");

            return ResponseEntity.ok(indexing);
        }





    }

}
