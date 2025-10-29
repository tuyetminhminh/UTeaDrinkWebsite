package net.codejava.utea.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponse {
    private List<Candidate> candidates;
    private UsageMetadata usageMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private GeminiRequest.Content content;
        private String finishReason;
        private Integer index;
        private List<SafetyRating> safetyRatings;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyRating {
        private String category;
        private String probability;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
    }
}

