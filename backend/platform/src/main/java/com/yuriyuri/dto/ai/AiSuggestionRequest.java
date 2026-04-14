package com.yuriyuri.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiSuggestionRequest {
    private String type;

    @NotBlank
    private String description;

    private String imageUrl;

}
