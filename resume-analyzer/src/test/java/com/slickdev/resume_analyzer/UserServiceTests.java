package com.slickdev.resume_analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.slickdev.resume_analyzer.Constants.TestConstants;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.repositories.UserRepository;
import com.slickdev.resume_analyzer.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository repository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UUID fakeID;
    private UUID fakeWrongID;

    @BeforeEach
    void setup() {
        user = new User(
            TestConstants.FAKEUSER_USERNAME_STRING,
            TestConstants.FAKEUSER_EMAIL_STRING,
            TestConstants.FAKEUSER_PASSWORD_STRING
        );
        fakeID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");
        fakeWrongID = UUID.fromString("54321678-1234-1234-1234-1234567890ab");
        user.setId(fakeID);
    }

    @Test
    public void saveUser_ShouldReturnSavedUserWithEncodedPassword() {
        when(passwordEncoder.encode(anyString())).thenReturn(TestConstants.FAKE_ENCODED_PASSWORD);
        when(repository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertEquals(user.getUserName(), savedUser.getUserName());
        assertEquals(TestConstants.FAKE_ENCODED_PASSWORD, savedUser.getPassword());
        verify(passwordEncoder).encode(TestConstants.FAKEUSER_PASSWORD_STRING);
    }

    @Test
    public void getUserById_ShouldReturnUser_WhenUserExists() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(user));

        User result = userService.getUser(TestConstants.FAKE_UUID_STRING);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
    }

    @Test
    public void getUserById_ShouldThrowException_WhenUserDoesNotExist() {
        when(repository.findById(fakeWrongID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            userService.getUser(TestConstants.FAKE_WRONG_UUID_STRING)
        );
    }

    @Test
    public void getUserByUsernameOrEmail_ShouldReturnUser() {
        when(repository.findByUserNameOrEmail(anyString(), anyString())).thenReturn(Optional.of(user));

        User userByEmail = userService.getUserByUsernameOrEmail(TestConstants.FAKEUSER_EMAIL_STRING);
        User userByUsername = userService.getUserByUsernameOrEmail(TestConstants.FAKEUSER_USERNAME_STRING);

        assertNotNull(userByEmail);
        assertNotNull(userByUsername);
        assertEquals(user.getUserName(), userByEmail.getUserName());
        assertEquals(user.getUserName(), userByUsername.getUserName());
    }

    @Test
    public void getUserByUsernameOrEmail_ShouldThrowException_WhenNotFound() {
        when(repository.findByUserNameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            userService.getUserByUsernameOrEmail(TestConstants.FAKEUSER_USERNAME_STRING)
        );
    }

    @Test
    public void getUserInfo_ShouldReturnUserInfo_WhenUserExists() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(user));

        UserResponseDto userInfo = userService.getUserinfo(TestConstants.FAKE_UUID_STRING);

        assertNotNull(userInfo);
        assertEquals(user.getUserName(), userInfo.getUserName());
    }

    @Test
    public void getUserInfo_ShouldThrowException_WhenUserDoesNotExist() {
        when(repository.findById(fakeWrongID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            userService.getUserinfo(TestConstants.FAKE_WRONG_UUID_STRING)
        );
    }

    @Test
    public void deleteUser_ShouldDelete_WhenUserExists() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(user));

        userService.deleteUser(TestConstants.FAKE_UUID_STRING);

        verify(repository).deleteById(fakeID);
    }

    @Test
    public void deleteUser_ShouldThrowException_WhenUserDoesNotExist() {
        when(repository.findById(fakeWrongID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            userService.deleteUser(TestConstants.FAKE_WRONG_UUID_STRING)
        );
    }
}
