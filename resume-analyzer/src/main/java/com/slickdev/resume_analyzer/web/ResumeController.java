package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.service.ResumeService;

import lombok.AllArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;






@AllArgsConstructor
@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;
    
    @PostMapping("/upload")
    public ResponseEntity<ResumeIdResponse> uploadResume(@RequestParam MultipartFile file) {
        return new ResponseEntity<>(resumeService.parseFile(file, null), HttpStatus.OK);
    }

    @PostMapping("/{userId}/upload")
    public ResponseEntity<ResumeIdResponse> uploadUserResume(@PathVariable String userId, @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(resumeService.parseFile(file, userId), HttpStatus.OK);
    }

    @GetMapping("/analyze/{id}")
    public ResponseEntity<String> analyzeResume(@RequestBody String jobDescription, @PathVariable String id) {
        return new ResponseEntity<>(resumeService.analyzeResume(id, jobDescription) ,HttpStatus.OK);
    }

    //Create /all path for resume histories
    //There should be analysis history.
}
