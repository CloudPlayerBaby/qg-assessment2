package com.yuriyuri.service;

import com.yuriyuri.dto.ai.AiSuggestionRequest;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public interface AiService {
    Flux<String> polishDescription(Long userId,AiSuggestionRequest req) throws MalformedURLException;
    String chat(String message);
    String generateAnalysisReport(List<Map<String, Object>> lostPlaceStats, 
                              List<Map<String, Object>> lostItemStats,
                              List<Map<String, Object>> foundPlaceStats,
                              List<Map<String, Object>> foundItemStats);
}
