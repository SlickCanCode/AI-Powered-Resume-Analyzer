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

    @Column(name = "Source_url")
    private String source_url;

    @Lob
    @NotBlank(message = "Content should not be blank")
    @Column(name = "File_Content")
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
