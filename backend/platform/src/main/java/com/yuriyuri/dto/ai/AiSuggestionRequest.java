package com.yuriyuri.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiSuggestionRequest {
    @NotBlank
    private String description;
}
