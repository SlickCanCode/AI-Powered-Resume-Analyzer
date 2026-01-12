package com.slickdev.resume_analyzer.web;



import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.AuthResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.reponses.ResumeResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;
import com.slickdev.resume_analyzer.service.ResumeService;
import com.slickdev.resume_analyzer.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;




@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final ResumeService resumeService;

    @PostMapping("")
    public ResponseEntity<AuthResponse> saveUser(@Valid @RequestBody User user) {
        return new ResponseEntity<AuthResponse>(userService.registerUser(user), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUser(@PathVariable String id) {
		return new ResponseEntity<>(userService.getUserinfo(id), HttpStatus.OK);
	}

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> editUser(@PathVariable String id, @Valid @RequestBody UpdateuserRequest request) {
        return  new ResponseEntity<>(userService.updateUser(id,request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable String id) {
       userService.deleteUser(id);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }   

    @PostMapping("/{id}/resumes")
    public ResponseEntity<ResumeIdResponse> uploadUserResume(@PathVariable String id, @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(resumeService.parseFile(file, id), HttpStatus.OK);
    }

    @GetMapping("/{id}/resumes")
    public ResponseEntity<List<ResumeResponse>> getAllresumes(@PathVariable String id) {
        return new ResponseEntity<>(resumeService.getUserResumes(id), HttpStatus.OK);
    }

}
