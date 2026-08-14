package com.slickdev.resume_analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import com.slickdev.resume_analyzer.exception.DuplicateResourceException;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.reponses.UserResponseDto;
import com.slickdev.resume_analyzer.repositories.UserRepository;
import com.slickdev.resume_analyzer.requests.UpdateuserRequest;
import com.slickdev.resume_analyzer.service.impl.UserServiceImpl;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
            TestConstants.FAKEUSER_FIRSTNAME_STRING,
			TestConstants.FAKEUSER_LASTNAME_STRING,
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

        assertEquals(user.getFirstName(), savedUser.getFirstName());
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
    public void getUserByEmail_ShouldReturnUser() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.of(user));

        User userByEmail = userService.getUserByEmail(TestConstants.FAKEUSER_EMAIL_STRING);

        assertNotNull(userByEmail);
        assertEquals(user.getEmail(), userByEmail.getEmail());
    }

    @Test
    public void getUserByEmail_ShouldThrowException_WhenNotFound() {
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            userService.getUserByEmail(TestConstants.FAKEUSER_EMAIL_STRING)
        );
    }

    @Test
    public void getUserInfo_ShouldReturnUserInfo_WhenUserExists() {
        when(repository.findById(fakeID)).thenReturn(Optional.of(user));

        UserResponseDto userInfo = userService.getUserinfo(TestConstants.FAKE_UUID_STRING);

        assertNotNull(userInfo);
        assertEquals(user.getEmail(), userInfo.getEmail());
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

    @Test
    public void updateUser_shouldReturnUpdatedUserinfo() {
        when(repository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(repository.existsByEmail(anyString())).thenReturn(false);

        UpdateuserRequest request = new UpdateuserRequest();
        request.setEmail("someEmail@gmail.com");
        request.setFirstName("joey");
        request.setLastName("sandals");
        UserResponseDto response =  userService.updateUser(TestConstants.FAKE_UUID_STRING, request);

        assertEquals(request.getFirstName(), response.getFirstName());
        assertEquals(request.getLastName(), response.getLastName());
    }

    @Test
    public void updateUser_shouldThrowException_whenUniqueinfoAlreadyExists() {
        when(repository.findById(any(UUID.class))).thenReturn(Optional.of(user));
        when(repository.existsByEmail(anyString())).thenReturn(true);
        
        UpdateuserRequest request = new UpdateuserRequest();
        request.setEmail("someEmail@gmail.com");
        request.setFirstName("joey");
        request.setLastName("sandals");

            assertThrows(DuplicateResourceException.class, () ->
            userService.updateUser(TestConstants.FAKE_UUID_STRING, request)
        );
    }

}
