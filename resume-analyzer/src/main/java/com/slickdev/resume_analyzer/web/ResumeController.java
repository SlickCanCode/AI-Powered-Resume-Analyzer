package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.service.ResumeService;

import lombok.AllArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;







@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;


        @PostMapping("/upload")
    public ResponseEntity<ResumeIdResponse> uploadUserResume(@CookieValue(name = "access_token") String jwt, @RequestParam("file") MultipartFile file) {
        System.out.println(jwt);
        return new ResponseEntity<>(resumeService.parseFile(file, jwt), HttpStatus.OK);
    }

    // @GetMapping("/{id}/resumes")
    // public ResponseEntity<List<ResumeResponse>> getAllresumes(@PathVariable String id) {
    //     return new ResponseEntity<>(resumeService.getUserResumes(id), HttpStatus.OK);
    // }

    // @PostMapping("/{id}/analyze")
    // public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@RequestBody String jobDescription, @PathVariable String id) {
    //     return new ResponseEntity<>(resumeService.analyzeResume(id, jobDescription) ,HttpStatus.OK);
    // }

    

}
