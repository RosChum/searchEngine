package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        HashMap<String, String> responseStatus = new HashMap<>();
        if (indexingService.statusIndexing()) {
            responseStatus.put("result", "false");
            responseStatus.put("error", "Индексация уже запущена");
            return ResponseEntity.ok().body(responseStatus);

        } else {
            indexingService.startIndexing();
            responseStatus.put("result", "true");
            return ResponseEntity.ok().body(responseStatus);
        }

    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        HashMap<String, String> responseStatus = new HashMap<>();
        if (indexingService.statusIndexing()) {
            responseStatus.put("result", "true");
            indexingService.stopIndexing();
            return ResponseEntity.ok().body(responseStatus);

        } else {
            responseStatus.put("result", "false");
            responseStatus.put("error", "Индексация не запущена");
            return ResponseEntity.ok().body(responseStatus);
        }


    }
}
