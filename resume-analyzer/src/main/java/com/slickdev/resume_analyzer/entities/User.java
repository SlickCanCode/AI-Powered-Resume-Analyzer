package com.slickdev.resume_analyzer.entities;


import java.util.List;
import java.util.UUID;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.slickdev.resume_analyzer.validation.SpecialCharactersValidation;
import com.slickdev.resume_analyzer.validation.password.PasswordLength;
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
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue()
    private UUID id;

    @NotNull
    @NotBlank(message = "First name cannot be blank")
    @SpecialCharactersValidation
    @UsernameLength
    @Column(name = "first_name")
    private String firstName;

    @NotNull
    @NotBlank(message = "Last name cannot be blank")
    @SpecialCharactersValidation
    @UsernameLength
    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @NotBlank(message = "Email cannot be blank")
    @Email
    @Column(name = "email", unique = true)
    private String email;
    
    @NotNull
    @NotBlank(message = "Password cannot be blank")
    @PasswordLength
    @Column(name = "password")
    private String password;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UploadedResume> resumes;

    public User (String firstname, String lastname, String email, String password) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.password = password;
    }
}
