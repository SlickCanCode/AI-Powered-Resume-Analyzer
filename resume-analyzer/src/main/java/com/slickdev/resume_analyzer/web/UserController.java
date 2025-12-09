package com.slickdev.resume_analyzer.web;



import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.slickdev.resume_analyzer.entities.User;
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

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> saveUser(@Valid @RequestBody User user) {
        userService.saveUser(user);
        URI location = URI.create("/user/" + user.getId());
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable String id) {
       userService.deleteUser(id);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }   

    //Create put mapping for user to edit their info 

}
