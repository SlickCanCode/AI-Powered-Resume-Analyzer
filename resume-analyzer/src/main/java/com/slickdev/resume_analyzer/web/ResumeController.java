package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RestController;

import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.service.ResumeService;

import lombok.AllArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;







@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@RequestBody String jobDescription, @PathVariable String id) {
        return new ResponseEntity<>(resumeService.analyzeResume(id, jobDescription) ,HttpStatus.OK);
    }

}
