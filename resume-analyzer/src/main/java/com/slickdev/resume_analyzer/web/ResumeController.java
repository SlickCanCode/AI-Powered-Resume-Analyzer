package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.service.ResumeServiceImpl;

import lombok.AllArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;




@AllArgsConstructor
@RestController
public class ResumeController {

    ResumeServiceImpl resumeService;
    
    @PostMapping("/analyze-resume")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file, @RequestParam String jobDescription) {
        resumeService.parseFile(file);
        return new ResponseEntity<>("returning the suggestions for a better " + jobDescription + "'s resume", HttpStatus.ACCEPTED);
    }

    
    
}
