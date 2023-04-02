package searchengine.dto;

import lombok.Data;

@Data
public class StatusRequest {
    private boolean result;
    private String error;

}
