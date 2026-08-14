package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.reponses.ResumeResponse;
import com.slickdev.resume_analyzer.requests.AnalysisRequest;
import com.slickdev.resume_analyzer.requests.JobMatchRequest;
import com.slickdev.resume_analyzer.service.ResumeService;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;








@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;


        @PostMapping("/upload")
    public ResponseEntity<ResumeDataResponse> uploadUserResume(@CookieValue(name = "access_token") String jwt, @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(resumeService.parseFile(file, jwt), HttpStatus.OK);
    }

        @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@RequestBody AnalysisRequest request , @PathVariable String id) {
        return new ResponseEntity<>(resumeService.analyzeResume(id, request.getJobDescription()) ,HttpStatus.OK);
    }

            @PostMapping("/{id}/analyze/job-match")
    public ResponseEntity<JobMatchResponse> analyzeJobMatch(@RequestBody JobMatchRequest request , @PathVariable String id) {
            return new ResponseEntity<>(resumeService.analyzeJobMatch(id, request.jobLink()), HttpStatus.OK);
        }

        @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAllResumes(@CookieValue(name = "access_token") String jwt) {
            return new ResponseEntity<>(resumeService.getAllResumes(jwt), HttpStatus.OK);
        }

    @GetMapping("/analyses")
    public ResponseEntity<List<AnalysisSummaryResponse>> getAllAnalyses(@CookieValue(name = "access_token") String jwt) {
        return ResponseEntity.ok(resumeService.getAllAnalyses(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDataResponse> getResumeData(
            @PathVariable String id,
            @CookieValue(name = "access_token") String jwt) {
        return ResponseEntity.ok(resumeService.getResumeData(id, jwt));
    }

    @GetMapping("/{id}/analyses")
    public ResponseEntity<List<ResumeAnalysisResponse>> getResumeAnalyses(
            @PathVariable String id,
            @CookieValue(name = "access_token") String jwt) {
        return ResponseEntity.ok(resumeService.getResumeAnalyses(id, jwt));
    }


    

}
