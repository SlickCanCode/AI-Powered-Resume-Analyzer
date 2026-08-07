package com.slickdev.resume_analyzer.entities;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.slickdev.resume_analyzer.validation.SpecialCharactersValidation;
import com.slickdev.resume_analyzer.validation.username.UsernameLength;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
@Entity
@AllArgsConstructor
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
    

    @Column(name = "password", nullable = true)
    private String password;

    @NotNull
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UploadedResume> resumes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VerificationToken> tokens = new ArrayList<>(); 

    @OneToOne(mappedBy = "user")
    private Subscription subscription;

    @OneToOne(mappedBy = "user")
    private SubscriptionUsage subscriptionUsage;





    public User (String firstname, String lastname, String email, String password) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.password = password;
    }

    public User(String firstname, String lastname, String email, boolean emailVerified) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public void addToken(VerificationToken token) {
    tokens.add(token);
    token.setUser(this);
    }
}
