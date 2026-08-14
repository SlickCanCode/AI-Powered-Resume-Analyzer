package com.slickdev.resume_analyzer.reponses;

import java.time.LocalDateTime;

public record AnalysisSummaryResponse(String resumeName, LocalDateTime dateTime, int score, int atsScore) {
}
