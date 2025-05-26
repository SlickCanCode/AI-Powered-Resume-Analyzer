package com.slickdev.resume_analyzer.entities;


import java.util.List;
import java.util.UUID;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.slickdev.resume_analyzer.validation.SpecialCharactersValidation;
import com.slickdev.resume_analyzer.validation.email.UniqueEmail;
import com.slickdev.resume_analyzer.validation.fullname.FullNameLength;
import com.slickdev.resume_analyzer.validation.password.PasswordLength;
import com.slickdev.resume_analyzer.validation.username.UniqueUsername;
import com.slickdev.resume_analyzer.validation.username.UsernameLength;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue()
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @NonNull
    @NotBlank(message = "Fullname cannot be blank")
    @FullNameLength
    @Column(name = "fullName")
    private String fullName;

    @NonNull
    @NotBlank(message = "Username cannot be blank") 
    @SpecialCharactersValidation
    @UniqueUsername
    @UsernameLength
    @Column(name = "userName")
    private String userName;

    @NonNull
    @NotBlank(message = "Email cannot be blank")
    @Email
    @UniqueEmail
    @Column(name = "email")
    private String email;

    
    @NonNull
    @NotBlank(message = "Password cannot be blank")
    @PasswordLength
    @Column(name = "password")
    private String password;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UploadedResume> resumes;

}
