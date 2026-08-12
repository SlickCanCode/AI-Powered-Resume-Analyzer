package com.slickdev.resume_analyzer.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
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
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.exception.FileProcessingException;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.reponses.ResumeIdResponse;
import com.slickdev.resume_analyzer.reponses.ResumeResponse;
import com.slickdev.resume_analyzer.repositories.ResumeDataRepository;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.service.ResumeService;
import com.slickdev.resume_analyzer.service.constants.ServiceConstants;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class ResumeServiceImpl implements ResumeService{


    private final ResumeRepository resumeRepository;
    private final GeminiService geminiService;
    private final UserServiceImpl userService;
    private final JwtServiceImpl jwtService;
    private final ResumeDataRepository resumeDataRepository;

    @Override
    public UploadedResume saveResume(UploadedResume resume) {
        return resumeRepository.save(resume);
    }

    @Override
    public UploadedResume findById(String id) {
        UUID refinedId = UUID.fromString(formatUUID(id));
        return unwrapResume(resumeRepository.findById(refinedId), refinedId);
    }

    @Override
    public List<UploadedResume> getAnalyzedResumes(String userId) {
        UUID refinedUserId = UUID.fromString(formatUUID(userId));
        return resumeRepository.findByUserIdAndAnalysisCountGreaterThan(refinedUserId, 0);
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
    public ResumeDataResponse parseFile(MultipartFile file, String jwt) {
        String userId = jwtService.extractUserId(jwt);
        User user = userService.getUser(userId);
        try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream())) {
            inputStream.mark(Integer.MAX_VALUE);
            String fileType = file.getContentType();
            byte[] bytes = inputStream.readAllBytes();
            InputStream safeStream = new ByteArrayInputStream(bytes);

            //Necessary Checks
            if (fileType == null) {
                throw new IllegalArgumentException("File type not supported");
            }
            boolean isPdf = fileType.equals("application/pdf");

            boolean isWordDocument =
                    fileType.equals("application/msword") ||
                    fileType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            boolean isImage = fileType.startsWith("image/");

            if (!isPdf && !isWordDocument && !isImage) {
                throw new IllegalArgumentException(
                        "Unsupported file type. Please upload a PDF, Word document, or image file."
                );
            }

            // Extra Security for malformed pdfs 
            inputStream.reset();
            if(isPdf) {
                if(!isStrictPdf(inputStream)) {
                    throw new FileProcessingException("Unable to parse file: Bad/Malformed pdf detected");
                }
            }

            //parse content
            AutoDetectParser parser = new AutoDetectParser();
            TesseractOCRConfig ocrConfig = new TesseractOCRConfig();//OCR Support
            ocrConfig.setLanguage("eng");

            ParseContext context = new ParseContext();
            context.set(Parser.class, parser);
            context.set(TesseractOCRConfig.class, ocrConfig);

            BodyContentHandler handler = new BodyContentHandler(-1); //For unlimited body size
            Metadata metadata = new Metadata();


            parser.parse(safeStream, handler, metadata, context);

            String fileName =file.getOriginalFilename();
            String parsedContent = handler.toString();
            ResumeData resumeData = geminiService.parseResume(parsedContent);

            UploadedResume resume = resumeRepository.save(UploadedResume.builder()
            .filename(fileName)
            .fileType(fileType)
            .user(user)
            .parsedContent(parsedContent)
            .build());
            resumeData.setResume(resume);
            resumeDataRepository.save(resumeData);

        return ResumeDataResponse.builder()
                .resumeId(resume.getId().toString())
                .fullName(resumeData.getFullName())
                .email(resumeData.getEmail())
                .phone(resumeData.getPhone())
                .skills(resumeData.getSkills())
                .education(resumeData.getEducation())
                .experience(resumeData.getExperience())
                .build();

        }catch (IOException | TikaException | SAXException e) {
            throw new FileProcessingException("Unable to parse file:" + e.getMessage());
        }
    }


    // public ResumeDataResponse getResumeData(String resumeId, String jwt) {
    //     String userId = jwtService.extractUserId(jwt);
    //     UUID refinedResumeId = UUID.fromString(formatUUID(resumeId));
    //     UUID refinedUserId = UUID.fromString(formatUUID(userId));

    //     if(!resumeRepository.existsByIdAndUserId(refinedResumeId, refinedUserId)) {
    //         throw new EntityNotFoundException(refinedResumeId, UploadedResume.class);
    //     }

    //     ResumeData resumeData = resumeDataRepository.findByResumeId(refinedResumeId)
    //             .orElseThrow(() -> new EntityNotFoundException(refinedResumeId, ResumeData.class));

    //     return ResumeDataResponse.builder()
    //             .fullName(resumeData.getFullName())
    //             .email(resumeData.getEmail())
    //             .phone(resumeData.getPhone())
    //             .skills(resumeData.getSkills())
    //             .education(resumeData.getEducation())
    //             .experience(resumeData.getExperience())
    //             .build();
    // }

//     @Override
//     public ResumeAnalysisResponse analyzeResume(String id, String jobDescription) {
//         UploadedResume resume = findById(id);
//         String resumeContent = resume.getContent();
//         String api_URL = ServiceConstants.API_URL;

//         //Build request
//         String prompt = promptBuilder.buildPrompt(resumeContent, jobDescription);
//         Map<String,Object> requestBody = buildRequestBody(prompt);
        
//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);

//         //Wraping body and headers together
//         HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
//         //send request
//         try {
//                     ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
//                         api_URL, 
//                         HttpMethod.POST, 
//                         entity, 
//                         new ParameterizedTypeReference<Map<String, Object>>() {});

//                         String aiResponse =  extractTextFromResponse(response);
//                         String aiResponseCleaned = aiResponse
//                                 .replaceAll("(?s)```\\w*\\n", "")
//                                 .replaceAll("```", "")
//                                 .replaceFirst("(?i)^json:\\s*", "")
//                                 .trim();
//                         resume.setAnalysis(aiResponseCleaned);
//                         resumeRepository.save(resume);
//                         ObjectMapper mapper = new ObjectMapper();
//                         ResumeAnalysisResponse result = mapper.readValue(aiResponseCleaned, ResumeAnalysisResponse.class);
//                         return result;
                
//         } catch (HttpClientErrorException | HttpServerErrorException e) {
//             throw new RuntimeException("Gemini API Error: " + e.getMessage());
//     } catch (ResourceAccessException e) {
//     // Timeout, no connection
//     throw new RuntimeException("Connection Error: " + e.getMessage());
//     } catch (JsonProcessingException e) {
//         throw new RuntimeException("Mapper Error: " + e.getMessage());
//     }
//  } 


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
