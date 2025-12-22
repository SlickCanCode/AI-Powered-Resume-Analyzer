package com.slickdev.resume_analyzer.service.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
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
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.exception.FileProcessingException;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.service.ResumeService;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ResumeServiceImpl implements ResumeService{

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private final ResumeRepository resumeRepository;
    private final UserServiceImpl userService;
    private final RestTemplate restTemplate;
    private final PromptBuilder promptBuilder;

    @Override
    public UploadedResume saveResume(UploadedResume resume) {
        return resumeRepository.save(resume);
    }

    @Override
    public UploadedResume findResumeByContentAndUser(User user, String content) {
        Optional<UploadedResume> resume = resumeRepository.findByUserAndContent(user, content);
        if (resume.isPresent()) return resume.get();
        else throw new EntityNotFoundException(user.getUserName(), UploadedResume.class);
    }

    @Override
    public UploadedResume findByContent(String content) {
        Optional<UploadedResume> resume = resumeRepository.findByContent(content);
        if (resume.isPresent()) return resume.get();
        else throw new EntityNotFoundException("the content", UploadedResume.class);
    }

    @Override
    public UploadedResume findById(String id) {
        UUID refinedId = UUID.fromString(formatUUID(id));
        return unwrapResume(resumeRepository.findById(refinedId), refinedId);
    }

    static UploadedResume unwrapResume(Optional<UploadedResume> entity, UUID id) {
        if (entity.isPresent()) return entity.get();
        else throw new EntityNotFoundException(id, UploadedResume.class);
    }

    private String formatUUID(String raw) {
    return raw.replaceFirst(
        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
        "$1-$2-$3-$4-$5"
    );
    }

    public boolean isStrictPdf(InputStream inputStream) {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
            document.getDocumentCatalog(); // force catalog parsing
            document.getPages().getCount(); // force page tree parsing

            for (PDPage page : document.getPages()) { //Extra Strict
                    page.getContents();
                }
            return true;

        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public ResumeIdResponse parseFile(MultipartFile file, String userId) {
        try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream())) {
            inputStream.mark(Integer.MAX_VALUE);
            Tika tika = new Tika();
            String fileType = tika.detect(inputStream);

            //Extra Security for malformed pdfs 
            // if(fileType.equals("application/pdf")) {
            //     inputStream.reset();
            //     log.info("PDF DETECTED");
            //     if(!isStrictPdf(inputStream)) {
            //         throw new FileProcessingException("Unable to parse file: Bad/Malformed pdf detected");
            //     } else {
            //         log.info("PDF PASSED VALIDATION");
            //     }
            //     inputStream.reset();
            // }

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
                
                if (userId != null)//for authenticated users 
                {
                    User user = userService.getUser(userId);
                    if (!resumeRepository.existsByUserAndContent(user, parsedContent)) {
                        UploadedResume resume = saveResume(new UploadedResume(fileName, fileType, parsedContent, data, user));
                            if (user.getResumes()!=null ) { //avoid null pointer exception
                                user.getResumes().add(resume);
                            } else {
                                user.setResumes(Arrays.asList(resume));
                            }
                    }
                    UploadedResume resume = findResumeByContentAndUser(user, parsedContent);
                    return new ResumeIdResponse(resume.getId().toString());
                } 
                 //For Unauthenticated users
                 if (!resumeRepository.existsByContent(parsedContent)) { 
                       return new ResumeIdResponse(saveResume(new UploadedResume(fileName, fileType, parsedContent, data)).getId().toString()); //save in the database only if the content doesn't exists in the database
                 }
                 
                 return new ResumeIdResponse(findByContent(parsedContent).getId().toString());//return the resume's id 
            
        }catch (IOException | TikaException | SAXException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            log.error("Failed to parse file", cause);
            throw new FileProcessingException("Unable to parse file:" + e.getMessage());
        }
    }



    @Override
    public ResumeAnalysisResponse analyzeResume(String id, String jobDescription) {
        String resumeContent = findById(id).getContent();
        String api_URL = ServiceConstants.API_URL;

        //Build request
        String prompt = promptBuilder.buildPrompt(resumeContent, jobDescription);
        Map<String,Object> requestBody = buildRequestBody(prompt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Wraping body and headers together
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        //send request
        try {
                    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        api_URL, 
                        HttpMethod.POST, 
                        entity, 
                        new ParameterizedTypeReference<Map<String, Object>>() {});
                        ObjectMapper mapper = new ObjectMapper();
                        String aiResponse =  extractTextFromResponse(response);
                        String aiResponseCleaned = aiResponse
                                .replaceAll("(?s)```.*?\\n", "") 
                                .replaceAll("```", "")           
                                .trim();
                        ResumeAnalysisResponse result = mapper.readValue(aiResponseCleaned, ResumeAnalysisResponse.class);
                        return result;
                
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Gemini API Error: " + e.getMessage());
    } catch (ResourceAccessException e) {
    // Timeout, no connection
    throw new RuntimeException("Connection Error: " + e.getMessage());
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Mapper Error: " + e.getMessage());
    }
 } 



    private Map<String, Object> buildRequestBody(String prompt) {
            Map<String, String> textPart = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(textPart));
            return Map.of("contents", List.of(content));
        }



    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(ResponseEntity<Map<String, Object>> response) {
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");
                return parts.get(0).get("text");
            }
        }
        return "An Unexpected Error Occured!, pls try again";
    }

}
