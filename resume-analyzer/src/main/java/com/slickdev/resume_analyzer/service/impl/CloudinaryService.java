package com.slickdev.resume_analyzer.service.impl;



import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {
    
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SuppressWarnings("unchecked")
    public String uploadResume(MultipartFile file, String userId, String contentType) {
        try {

            boolean isPdf = contentType.equals("application/pdf");

            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", "resumes/" + userId,
                    "resource_type", isPdf ? "raw" : "image",
                    "use_filename", true,
                    "unique_filename", true
            );

            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), options);

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload resume", e);
        }
    }
}
