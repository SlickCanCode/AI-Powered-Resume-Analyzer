package com.slickdev.resume_analyzer.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class UploadedResume {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "File_name")
    private String filename;

    @Column(name = "File_type")
    private String contentType;

    @Lob
    @Column(name = "file_data")
    
    private byte[] data;

    @Lob
    @NotBlank(message = "For some reason the content appears blank")
    @Column(name = "File_Content")
    private String content;    

    public UploadedResume(String fileName, String fileType, byte[] data, String parsedContent) {
        this.filename = fileName;
        this.contentType = fileType;
        this.data = data;
        this.content = parsedContent;
    }
}
