package com.example.kgplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KosExtractRequest {
    private String extractType = "all";
    private String language = "zh";
    private String model;
    private String schema;
    private String text;
}
