package com.slickdev.resume_analyzer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.DuplicateResourceException;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.repositories.UserRepository;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;
import com.slickdev.resume_analyzer.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private static final UUID USER_ID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");
    private static final String JWT = "jwt";

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private OtpService otpService;
    @InjectMocks private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Ada", "Lovelace", "ada@example.com", "password");
        user.setId(USER_ID);
    }

    @Test
    void saveUserEncodesPasswordAndPersistsUniqueEmail() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);

        assertEquals(user, userService.saveUser(user));
        assertEquals("encoded", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void saveUserRejectsAnExistingVerifiedEmail() {
        User existing = new User("Ada", "Lovelace", user.getEmail(), "encoded");
        existing.setEmailVerified(true);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(existing));

        assertThrows(DuplicateResourceException.class, () -> userService.saveUser(user));
    }

    @Test
    void updateUserRejectsAnotherUsersEmail() {
        UpdateuserRequest request = new UpdateuserRequest();
        request.setFirstName("Grace");
        request.setLastName("Hopper");
        request.setEmail("taken@example.com");
        when(jwtService.extractUserId(JWT)).thenReturn(USER_ID.toString());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.updateUser(JWT, request));
    }

    @Test
    void updateUserChangesOnlyTheAuthenticatedUser() {
        UpdateuserRequest request = new UpdateuserRequest();
        request.setFirstName("Grace");
        request.setLastName("Hopper");
        request.setEmail("grace@example.com");
        when(jwtService.extractUserId(JWT)).thenReturn(USER_ID.toString());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        assertEquals("grace@example.com", userService.updateUser(JWT, request).getEmail());
        assertEquals("Grace", user.getFirstName());
    }

    @Test
    void deleteUserDeletesTheAuthenticatedUserOnce() {
        when(jwtService.extractUserId(JWT)).thenReturn(USER_ID.toString());
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        userService.deleteUser(JWT);

        verify(userRepository).delete(user);
    }

    @Test
    void getUserThrowsNotFoundForUnknownId() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUser(USER_ID.toString()));
    }
}
