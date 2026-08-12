package com.slickdev.resume_analyzer.service.impl;

import com.slickdev.resume_analyzer.repositories.ResumeAnalysisRepository;
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
import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
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
    private final ResumeAnalysisRepository resumeAnalysisRepository;

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

    @Override
    public ResumeAnalysisResponse analyzeResume(String id, String jobDescription) {
        UploadedResume resume = findById(id);
        String resumeContent = resume.getParsedContent();
        ResumeAnalysis analysis = geminiService.analyzeResume(resumeContent, jobDescription);
        analysis.setResume(resume);
        resume.setAnalysisCount(resume.getAnalysisCount() + 1);
        resumeAnalysisRepository.save(analysis);

        return new ResumeAnalysisResponse(analysis.getId().toString(), analysis.getOverallScore(), analysis.getAtsScore(),
                     analysis.getKeywordScore(), analysis.getStrengths(), analysis.getMissingKeywords(), analysis.getMissingKeywords(), analysis.getGrammarIssues(), analysis.getRecommendations());

 } 


}
