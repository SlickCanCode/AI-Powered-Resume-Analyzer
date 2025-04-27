package com.slickdev.resume_analyzer.service;

import java.io.BufferedInputStream;
import java.util.*;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.exception.FileProcessingException;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ResumeServiceImpl implements ResumeService{

    private ResumeRepository resumeRepository;
    private RestTemplate restTemplate;
    
    @Override
    public UploadedResume saveResume(UploadedResume resume) {
        return resumeRepository.save(resume);
    }

    @Override
    public UploadedResume findUploadedResume(Long id) {
        return unwrapUser(resumeRepository.findById(id), id);
    }

    @Override
    public void parseFile(MultipartFile file) {
        try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream())) {
            inputStream.mark(Integer.MAX_VALUE);
            Tika tika = new Tika();
            String fileType = tika.detect(inputStream);

            inputStream.reset();

            //parse content
            AutoDetectParser parser = new AutoDetectParser();
            TesseractOCRConfig ocrConfig = new TesseractOCRConfig();//OCR Support
            ocrConfig.setLanguage("eng");

            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);
            context.set(TesseractOCRConfig.class, ocrConfig);

            BodyContentHandler handler = new BodyContentHandler(-1); //For unlimited body size
            Metadata metadata = new Metadata();

            parser.parse(inputStream, handler, metadata, context);
            String fileName =file.getOriginalFilename();
            byte[] data = file.getBytes();
            String parsedContent = handler.toString();
            
            if (!resumeRepository.existsByContent(parsedContent)) {
                      saveResume(new UploadedResume(fileName, fileType, data, parsedContent));
            }
            
        }catch (Exception e) {
            e.printStackTrace();
            throw new FileProcessingException(e.getMessage());
        }
        
    }

    static UploadedResume unwrapUser(Optional<UploadedResume> entity, Long id) {
        if (entity.isPresent()) return entity.get();
        else throw new EntityNotFoundException(id, UploadedResume.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String analyzeResume(Long id, String jobDescription) {
        String resumeContent = unwrapUser(resumeRepository.findById(id), id).getContent();
        String api_URL = ServiceConstants.API_URL;

            
        //Build the request
        Map<String,Object> requestBody = new HashMap<>();//creates a map, json like structure
        List<Map<String, Object>> contents = new ArrayList<>();
            //entry
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> textpart = new HashMap<>();
        textpart.put("text","You are an expert career coach. Compare the resume below to the job description and reply ONLY with a JSON object like this: {\r\n" + //
                        "  \\\"score\\\": (number between 0 and 100),\r\n" + //
                        "  \\\"strengths\\\": [list of strengths],\r\n" + //
                        "  \\\"weaknesses\\\": [list of weaknesses],\r\n" + //
                        "  \\\"improvementSuggestions\\\": [list of improvements]\r\n" + //
                        "}  Resume:\n" + resumeContent + "\n\nJob Description:\n" + jobDescription );
        parts.add(textpart);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);
        contents.add(content);

        requestBody.put("contents", contents);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Wrap body and headers together
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        //send the request
        try {
                    ResponseEntity<Map> response = restTemplate.exchange(api_URL, HttpMethod.POST, entity, Map.class);

                    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                        //the Gemini API sends results under a candidates list
                        
                        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            
                        if (candidates !=null && !candidates.isEmpty()) {
                            //the first candidate (usually the most relevant AI response).
                            Map<String, Object> firstCandidate = candidates.get(0);
                            Map<String, Object> responseContent = (Map<String, Object>) firstCandidate.get("content");
                            List<Map<String, String>> responseParts = (List<Map<String, String>>) responseContent.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                return responseParts.get(0).get("text"); // The AI's reply
                        }
                    }
                }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
             // API errors like 400, 500
            throw new RuntimeException("Gemini API Error: " + e.getMessage());
    } catch (ResourceAccessException e) {
    // Timeout, no connection
    throw new RuntimeException("Connection Error: " + e.getMessage());
    
    }

    return "No response from Gemini.";
} 

}
