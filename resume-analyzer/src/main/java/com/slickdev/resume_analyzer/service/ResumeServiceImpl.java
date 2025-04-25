package com.slickdev.resume_analyzer.service;

import java.io.BufferedInputStream;
import java.util.Optional;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
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
}
