package com.slickdev.resume_analyzer.web;




import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slickdev.resume_analyzer.reponses.RegisterResponse;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.requests.RegisterRequest;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;
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

    // implement redirecting to veriy otp page if the user is not verified and trying to register -
    // with unverified email. 
    @PostMapping("")
    public ResponseEntity<RegisterResponse> saveUser(@Valid @RequestBody RegisterRequest user) {
        return new ResponseEntity<RegisterResponse>(userService.registerUser(user), HttpStatus.CREATED);
    }

    @GetMapping("/me")
	public ResponseEntity<UserResponseDto> getUser(@CookieValue(name = "access_token") String jwt) {
		return new ResponseEntity<>(userService.getUserinfo(jwt), HttpStatus.OK);
	}

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> editUser(@CookieValue(name = "access_token") String jwt, @Valid @RequestBody UpdateuserRequest request) {
        return  new ResponseEntity<>(userService.updateUser(jwt,request), HttpStatus.OK);
    }

    @DeleteMapping("/me")
    public ResponseEntity<HttpStatus> deleteUser(@CookieValue(name = "access_token") String jwt) {
       userService.deleteUser(jwt);
       return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }   
    

}
