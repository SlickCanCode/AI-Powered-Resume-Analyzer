package com.slickdev.resume_analyzer.reponses;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalysisSummaryResponse(UUID id, String resumeName, LocalDateTime dateTime, int score, int atsScore) {
}
