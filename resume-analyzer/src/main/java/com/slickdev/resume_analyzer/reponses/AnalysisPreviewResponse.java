package com.slickdev.resume_analyzer.reponses;

import java.time.LocalDateTime;
import java.util.UUID;

public class AnalysisPreviewResponse {

    private String id;
    private String resumeId;
    private String fileName;
    private int resumeScore;
    private int atsScore;
    private String date;

    // Constructor with all arguments
    public AnalysisPreviewResponse(
            String id,
            String resumeId,
            String fileName,
            int resumeScore,
            int atsScore,
            String date
    ) {
        this.id = id;
        this.resumeId = resumeId;
        this.fileName = fileName;
        this.resumeScore = resumeScore;
        this.atsScore = atsScore;
        this.date = date;
    }

    // Constructor that accepts LocalDateTime
    public AnalysisPreviewResponse(
            String id,
            String resumeId,
            String fileName,
            int resumeScore,
            int atsScore,
            LocalDateTime dateTime
    ) {
        this(
            id,
            resumeId,
            fileName,
            resumeScore,
            atsScore,
            dateTime != null ? dateTime.toString() : null
        );
    }

    public AnalysisPreviewResponse(
        UUID id,
        UUID resumeId,
        String fileName,
        int resumeScore,
        int atsScore,
        LocalDateTime dateTime
) {
    this(
        id.toString(),
        resumeId.toString(),
        fileName,
        resumeScore,
        atsScore,
        dateTime != null ? dateTime.toString() : null
    );
}

    // Getters
    public String getId() {
        return id;
    }

    public String getResumeId() {
        return resumeId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getResumeScore() {
        return resumeScore;
    }

    public int getAtsScore() {
        return atsScore;
    }

    public String getDate() {
        return date;
    }
}