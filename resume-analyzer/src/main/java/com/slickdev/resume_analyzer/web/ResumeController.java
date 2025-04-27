package com.slickdev.resume_analyzer.web;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.service.ResumeServiceImpl;

import lombok.AllArgsConstructor;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;





@AllArgsConstructor
@RestController
@RequestMapping("/resume")
public class ResumeController {

    ResumeServiceImpl resumeService;
    
    @PostMapping("/upload")
    public ResponseEntity<HttpStatus> uploadResume(@RequestParam("file") MultipartFile file) {
        resumeService.parseFile(file);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<String> analyzeReume(@PathVariable Long id, @RequestBody String jobDescription) {
        return new ResponseEntity<>(resumeService.analyzeResume(id, jobDescription), HttpStatus.OK);
    }
    

    
    
}
