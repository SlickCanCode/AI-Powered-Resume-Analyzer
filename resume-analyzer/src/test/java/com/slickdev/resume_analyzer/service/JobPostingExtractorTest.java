package com.slickdev.resume_analyzer.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.exception.BadRequestException;
import com.slickdev.resume_analyzer.service.impl.JobPostingExtractor;

import okhttp3.OkHttpClient;

class JobPostingExtractorTest {
    private final JobPostingExtractor extractor = new JobPostingExtractor(new OkHttpClient(), new ObjectMapper());

    @Test
    void prefersJobPostingJsonLdOverPageChrome() {
        String html = """
                <nav>Navigation noise</nav>
                <script type="application/ld+json">{"@context":"https://schema.org","@type":"JobPosting","title":"Backend Engineer","description":"Build reliable Java services."}</script>
                <main>Unrelated page content</main>
                """;

        String content = extractor.extractFromHtml(html);

        assertTrue(content.contains("Backend Engineer"));
        assertTrue(content.contains("Build reliable Java services"));
        assertFalse(content.contains("Navigation noise"));
    }

    @Test
    void fallsBackToVisibleJobContentAndRemovesNoise() {
        String content = extractor.extractFromHtml("<header>Header</header><main>Java developer with Spring Boot experience.</main><footer>Footer</footer>");

        assertTrue(content.contains("Spring Boot"));
        assertFalse(content.contains("Header"));
        assertFalse(content.contains("Footer"));
    }

    @Test
    void rejectsMissingAndNonHttpUrlsBeforeMakingARequest() {
        assertThrows(BadRequestException.class, () -> extractor.extract(""));
        assertThrows(BadRequestException.class, () -> extractor.extract("file:///etc/passwd"));
        assertThrows(BadRequestException.class, () -> extractor.extract("http://localhost/job"));
    }
}
