package com.slickdev.resume_analyzer.entities;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "uploaded_resumes")
public class UploadedResume {
    
    @Id
    @GeneratedValue()
    private UUID id;

    @Column(name = "file_name")
    private String filename;

    @Column(name = "file_type")
    private String contentType;

    @Column(name = "source_url")
    private String source_url;

    @Lob
    @NotBlank(message = "Content should not be blank")
    @Column(name = "file_Content")
    private String content;    

    @Lob
    @Column(name = "analysis")
    private String analysis;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;


    public UploadedResume (String fileName, String contentType, String content, String source_url, User user ) {
        this.filename = fileName;
        this.contentType = contentType;
        this.content = content;
        this.source_url = source_url;
        this.user = user;
    }

}
