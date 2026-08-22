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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import com.slickdev.resume_analyzer.entities.ResumeAnalysis;
import com.slickdev.resume_analyzer.entities.ResumeData;
import com.slickdev.resume_analyzer.entities.UploadedResume;
import com.slickdev.resume_analyzer.entities.User;
import com.slickdev.resume_analyzer.exception.EntityNotFoundException;
import com.slickdev.resume_analyzer.exception.FileProcessingException;
import com.slickdev.resume_analyzer.reponses.JobMatchResponse;
import com.slickdev.resume_analyzer.reponses.AnalysisSummaryResponse;
import com.slickdev.resume_analyzer.reponses.ResumeAnalysisResponse;
import com.slickdev.resume_analyzer.reponses.ResumeDataResponse;
import com.slickdev.resume_analyzer.reponses.ResumeResponse;
import com.slickdev.resume_analyzer.repositories.ResumeDataRepository;
import com.slickdev.resume_analyzer.repositories.ResumeRepository;
import com.slickdev.resume_analyzer.service.ResumeService;
import com.slickdev.resume_analyzer.service.SubscriptionService;

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
    private final JobPostingExtractor jobPostingExtractor;
    private final SubscriptionService subscriptionService;

    @Override
    public UploadedResume saveResume(UploadedResume resume) {
        return resumeRepository.save(resume);
    }

    @Override
    public UploadedResume findById(String id) {
        UUID refinedId = UUID.fromString(formatUUID(id));
        return unwrap(resumeRepository.findById(refinedId), refinedId, UploadedResume.class);
    }

    @Override
    public List<UploadedResume> getAnalyzedResumes(String userId) {
        UUID refinedUserId = UUID.fromString(formatUUID(userId));
        return resumeRepository.findByUserIdAndAnalysisCountGreaterThan(refinedUserId, 0);
    }

    public <T> T unwrap(Optional<T> entity, UUID id, Class<T> entityClass) {
        if (entity.isPresent()) {
            return entity.get();
        }

        throw new EntityNotFoundException(id, entityClass);
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
    public List<ResumeResponse> getAllResumes(String jwt) {
        UUID userId = UUID.fromString(formatUUID(jwtService.extractUserId(jwt)));

        return resumeRepository.findAllByUserId(userId)
                .stream()
                .map(resume -> new ResumeResponse(
                        resume.getId().toString(),
                        resume.getFilename(),
                        resume.getCreatedAt().toString(),
                        resume.getLatestScore(),
                        resume.getAnalysisCount()
                ))
                .toList();
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
                .location(resumeData.getLocation())
                .summary(resumeData.getCareerSummary())
                .onlineProfiles(resumeData.getOnlineProfiles())
                .skills(resumeData.getSkills())
                .education(resumeData.getEducation())
                .experience(resumeData.getExperience())
                .build();

        }catch (IOException | TikaException | SAXException e) {
            throw new FileProcessingException("Unable to parse file:" + e.getMessage());
        }
    }

    @Override
    public ResumeDataResponse getResumeData(String resumeId, String jwt) {
        UUID refinedResumeId = UUID.fromString(formatUUID(resumeId));
        UUID refinedUserId = UUID.fromString(formatUUID(jwtService.extractUserId(jwt)));
        ResumeData resumeData = resumeDataRepository.findByResumeIdAndResumeUserId(refinedResumeId, refinedUserId)
                .orElseThrow(() -> new EntityNotFoundException(refinedResumeId, ResumeData.class));

        return toResumeDataResponse(resumeData);
    }

    //This returns the first resume Analysis for now because i believe no resume should have more than one normal analyses unless the resume was edited.

    @Override
    public ResumeAnalysisResponse getResumeAnalyses(String resumeId, String jwt) {
        UUID refinedResumeId = UUID.fromString(formatUUID(resumeId));
        ResumeAnalysis analysis = unwrap(resumeAnalysisRepository.findFirstByResumeId(refinedResumeId), null, ResumeAnalysis.class);
        return new ResumeAnalysisResponse(analysis.getId().toString(), resumeId, analysis.getOverallScore(),
         analysis.getAtsScore(), analysis.getKeywordScore(), analysis.getStrengths(), analysis.getWeaknesses(),
          analysis.getExistingSkills(), analysis.getSkillsToDevelop(), analysis.getGrammarIssues(), analysis.getRecommendations());

    }

    @Override
    public List<AnalysisSummaryResponse> getAllAnalyses(String jwt) {
        UUID userId = UUID.fromString(formatUUID(jwtService.extractUserId(jwt)));
        return resumeAnalysisRepository.findAllSummariesByUserId(userId);
    }

    private ResumeDataResponse toResumeDataResponse(ResumeData resumeData) {
        return ResumeDataResponse.builder()
                .resumeId(resumeData.getResume().getId().toString())
                .fullName(resumeData.getFullName())
                .email(resumeData.getEmail())
                .phone(resumeData.getPhone())
                .location(resumeData.getLocation())
                .summary(resumeData.getCareerSummary())
                .onlineProfiles(resumeData.getOnlineProfiles())
                .skills(resumeData.getSkills())
                .education(resumeData.getEducation())
                .experience(resumeData.getExperience())
                .build();
    }

    private ResumeAnalysisResponse toResumeAnalysisResponse(ResumeAnalysis analysis, String resumeId) {
        return new ResumeAnalysisResponse(
                analysis.getId().toString(),
                resumeId,
                analysis.getOverallScore(),
                analysis.getAtsScore(),
                analysis.getKeywordScore(),
                analysis.getStrengths(),
                analysis.getWeaknesses(),
                analysis.getExistingSkills(),
                analysis.getSkillsToDevelop(),
                analysis.getGrammarIssues(),
                analysis.getRecommendations());
    }

    @Override
    public ResumeAnalysisResponse analyzeResume(String id, String jobDescription) {
        UploadedResume resume = findById(id);
        String userId = resume.getUser().getId().toString();
        
        // Check if user has remaining analysis quota
        if (!subscriptionService.hasAnalysisQuota(userId)) {
            throw new com.slickdev.resume_analyzer.exception.RateLimitException(
                    "You have reached your monthly analysis limit. Please upgrade your subscription."
            );
        }
        
        String resumeContent = resume.getParsedContent();
        ResumeAnalysis analysis = geminiService.analyzeResume(resumeContent, jobDescription);
        
        // Increment subscription usage
        subscriptionService.incrementAnalysisUsage(userId);
        
        // Update resume metadata
        analysis.setResume(resume);
        resume.increaseAnalysisCount();
        resume.setLatestScore(analysis.getOverallScore());
        resumeAnalysisRepository.save(analysis);

        return toResumeAnalysisResponse(analysis, resume.getId().toString());

 } 

    @Override
    public JobMatchResponse analyzeJobMatch(String id, String jobLink) {
        UploadedResume resume = findById(id);
        String userId = resume.getUser().getId().toString();

        if (!subscriptionService.hasAnalysisQuota(userId)) {
            throw new com.slickdev.resume_analyzer.exception.RateLimitException(
                    "You have reached your monthly analysis limit. Please upgrade your subscription."
            );
        }

        try {
            JobMatchResponse response = geminiService.analyzeJobMatch(resume.getParsedContent(), jobPostingExtractor.extract(jobLink));
            subscriptionService.incrementAnalysisUsage(userId);  
            resume.increaseAnalysisCount();        
            return response;
        } catch (JobPostingExtractor.JobPageUnavailableException exception) {
            JobMatchResponse response = geminiService.analyzeJobMatch(
                    resume.getParsedContent(),
                    "Job URL: " + jobLink + "\nRetrieve this job posting and extract its requirements before matching it to the resume.");
            resume.increaseAnalysisCount();
            return response;
        }
    }


}
