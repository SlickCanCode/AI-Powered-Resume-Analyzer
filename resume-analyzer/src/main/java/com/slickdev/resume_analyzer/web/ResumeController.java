package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.requests.AnalysisRequest;
import com.slickdev.resume_analyzer.service.ResumeService;

import lombok.AllArgsConstructor;


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

    // @GetMapping("/{id}/resumes")
    // public ResponseEntity<List<ResumeResponse>> getAllresumes(@PathVariable String id) {
    //     return new ResponseEntity<>(resumeService.getUserResumes(id), HttpStatus.OK);
    // }


    

}
