package com.slickdev.resume_analyzer.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UploadedResume {
    
    @Id
    @GeneratedValue()
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

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

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public UploadedResume (String fileName, String contentType, String content, byte[] data, User user ) {
        this.filename = fileName;
        this.contentType = contentType;
        this.content = content;
        this.data = data;
        this.user = user;
    }

    public UploadedResume (String fileName, String contentType, String content, byte[] data) {
        this.filename = fileName;
        this.contentType = contentType;
        this.content = content;
        this.data = data;
    }
   
}
