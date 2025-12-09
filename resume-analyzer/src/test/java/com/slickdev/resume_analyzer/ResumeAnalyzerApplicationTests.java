package com.slickdev.resume_analyzer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResumeAnalyzerApplicationTests {

	@Autowired
	MockMvc mockMvc;

	@Test
	void contextLoads() {
		assertNotNull(mockMvc);
	}

	@Test
	void testSuccessfulRegisterUser_shouldReturnSuccessfulandLocationHeader() throws Exception{

		RequestBuilder request = MockMvcRequestBuilders.post("/user/register")
			.contentType(APPLICATION_JSON)
			.content("{\"fullName\":\"john doe\", \"userName\":\"slickfeet\", \"email\":\"johndoe123@gmail.com\", \"password\":\"johndoe123\"}");

			mockMvc.perform(request)
				.andExpect(status().is2xxSuccessful())
				.andExpect(header().exists("Location"));
	}

	@Test
	void testUnsuccessfulRegisterUser_shouldReturnError() throws Exception{
		RequestBuilder request = MockMvcRequestBuilders.post("/user/register")
			.contentType(APPLICATION_JSON)
			.content("{\"fullName\":\"joh\", \"userName\":\"sli\", \"email\":\"johndoe123\", \"password\":\"jo\"}");

			mockMvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(APPLICATION_JSON));
	}

}
