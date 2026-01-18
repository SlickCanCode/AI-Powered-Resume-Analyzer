package com.slickdev.resume_analyzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.slickdev.resume_analyzer.Constants.TestConstants;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.repositories.UserRepository;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class ResumeAnalyzerApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ResumeRepository resumeRepository;

	@Autowired
	MockMvc mockMvc;

	private UploadedResume resume;
	private User user;

	@BeforeEach
	void setup() {

		user = new User(
			TestConstants.FAKEUSER_USERNAME_STRING,
			TestConstants.FAKEUSER_EMAIL_STRING,
			TestConstants.FAKEUSER_PASSWORD_STRING
		);
		userRepository.save(user);

		resume = new UploadedResume(
			TestConstants.RESUME_FILENAME,
			TestConstants.RESUME_CONTENT_TYPE,
			TestConstants.RESUME_CONTENT,
			TestConstants.SOURCE_URL,
			user
		);
		resumeRepository.save(resume);

	}

	@Test
	void contextLoads() {
		assertNotNull(mockMvc);
	}

	@Test
	void testSuccessfulRegisterUser_shouldReturnSuccessfulandAuthResponseBody() throws Exception{

		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/users")
			.contentType(APPLICATION_JSON)
			.content(TestConstants.signinRequest);

			mockMvc.perform(request)
				.andExpect(status().is2xxSuccessful())
				.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	void testUnsuccessfulRegisterUser_shouldReturnError() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/users")
			.contentType(APPLICATION_JSON)
			.content(TestConstants.badSigninRequest);

			mockMvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testSuccessfulGetUser_shouldReturnUserInfo() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/users/{userId}",user.getId().toString());

		mockMvc.perform(request)
			.andExpect(status().is2xxSuccessful())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testUnSuccessfulGetUser_shouldReturnUserNotFoundError() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/users/{userId}", TestConstants.FAKE_UUID_STRING);

		mockMvc.perform(request)
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testSuccessfulEditUser_shouldReturnEditedUserinfo() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/users/{userId}",user.getId().toString())
			.contentType(APPLICATION_JSON)
			.content(TestConstants.GOOD_EDITUSER_REQ);

		mockMvc.perform(request)
			.andExpect(status().is2xxSuccessful())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testUnSuccessfulEditUser__shouldReturnErrorIfInfoAlreadyInUse() throws Exception{
		User user2 = new User("slicky", "someemail@gmail.com", TestConstants.FAKEUSER_PASSWORD_STRING);
		userRepository.save(user2);
		RequestBuilder request = MockMvcRequestBuilders.put("/api/v1/users/{userId}",user.getId().toString())
			.contentType(APPLICATION_JSON)
			.content(TestConstants.BAD_EDITUSER_REQ);

		mockMvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testSuccesfulDeleteUser_shouldDeleteUser() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.delete("/api/v1/users/{userId}",user.getId().toString());

		mockMvc.perform(request)
			.andExpect(status().is2xxSuccessful());
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testUnSuccesfulDeleteUser_shouldThrowErrorIfUserNotFound() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.delete("/api/v1/users/{userId}",TestConstants.FAKE_UUID_STRING);

		mockMvc.perform(request)
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testSuccessfulUploadUserResume_shouldReturnResumeId() throws Exception{
		byte[] pdfBytes = Files.readAllBytes(
			Paths.get("src\\test\\java\\com\\slickdev\\resume_analyzer\\resources\\Adelanwa_Oreofe_Emmanuel_CV_Styled.pdf")
		);
		MockMultipartFile pdfFile = new MockMultipartFile(
			"file",              
			"resume.pdf",
			"application/pdf",
			pdfBytes
		);
		RequestBuilder request = MockMvcRequestBuilders.multipart("/api/v1/users/{userId}/resumes",user.getId().toString())
			.file(pdfFile)
    		.contentType(MediaType.MULTIPART_FORM_DATA);
			
		mockMvc.perform(request)
			.andExpect(status().is2xxSuccessful())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testUnsuccessfulUploadUserResume_shouldReturnError() throws Exception{
		MockMultipartFile pdfFile = new MockMultipartFile(
			"file",              
			"resume.pdf",
			"application/pdf",
			"Malformed PDF".getBytes()
		);
		RequestBuilder request = MockMvcRequestBuilders.multipart("/api/v1/users/{userId}/resumes",user.getId().toString())
			.file(pdfFile)
    		.contentType(MediaType.MULTIPART_FORM_DATA);
			
		mockMvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testGetUserResumes_shouldReturnUserResumeList() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.get("/api/v1/users/{userId}/resumes",user.getId().toString());
			
		mockMvc.perform(request)
			.andExpect(status().is2xxSuccessful())
			.andExpect(content().contentType(APPLICATION_JSON));
	}

	@Test
	@WithMockUser(username = "testuser", roles = {"USER"})
	void testAnalyzeResumes() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.post("/api/v1/resumes/{resumeId}/analyze",resume.getId().toString())
			.contentType(APPLICATION_JSON)
			.content(TestConstants.JOB_DESCRIPTION_REQUEST);
			
		mockMvc.perform(request)
			.andExpect(status().is2xxSuccessful())
			.andExpect(content().contentType(APPLICATION_JSON));
	}


}
