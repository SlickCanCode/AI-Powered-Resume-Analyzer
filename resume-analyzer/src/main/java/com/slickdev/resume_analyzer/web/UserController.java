package com.slickdev.resume_analyzer.web;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.reponses.AuthResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;

import com.slickdev.resume_analyzer.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;



@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getUser(@PathVariable String id) {
		return new ResponseEntity<>(userService.getUserinfo(id), HttpStatus.OK);
	}

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> saveUser(@Valid @RequestBody User user) {
        return new ResponseEntity<AuthResponse>(userService.registerUser(user), HttpStatus.CREATED);
    }

    @GetMapping("/verify-token")
    public ResponseEntity<HttpStatus> verifyToken() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
    

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable String id) {
       userService.deleteUser(id);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }   

    //Create put mapping for user to edit their info 




}
