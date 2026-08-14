package com.slickdev.resume_analyzer.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slickdev.resume_analyzer.exception.BadRequestException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class JobPostingExtractor {

    private static final int MAX_JOB_CONTENT_LENGTH = 50_000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; ResuMatch/1.0; +https://resumatch.app)";
    private static final Set<String> JOB_POSTING_TYPES = Set.of("JobPosting", "https://schema.org/JobPosting");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public JobPostingExtractor(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public String extract(String jobUrl) {
        URI uri = validateUrl(jobUrl);
        Request request = new Request.Builder()
                .url(uri.toString())
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new JobPageUnavailableException("The job page could not be retrieved.");
            }

            String html = response.body().string();
            if (html.isBlank()) {
                throw new JobPageUnavailableException("The job page did not contain any readable content.");
            }
            return extractFromHtml(html);
        } catch (IOException exception) {
            throw new JobPageUnavailableException("The job page could not be retrieved.", exception);
        }
    }

    public String extractFromHtml(String html) {
        Document document = Jsoup.parse(html);
        String jsonLdContent = extractJsonLdJobPosting(document);
        if (!jsonLdContent.isBlank()) {
            return jsonLdContent;
        }

        document.select("script, style, noscript, nav, footer, header, aside, iframe, svg, form, button").remove();
        Element content = document.selectFirst("main, article, [role=main], .job-description, #job-description, .description");
        String visibleText = (content == null ? document.body() : content).text();
        if (visibleText.isBlank()) {
            throw new BadRequestException("The job page did not contain a readable job description.");
        }
        return truncate(visibleText);
    }

    private String extractJsonLdJobPosting(Document document) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            try {
                JsonNode jobPosting = findJobPosting(objectMapper.readTree(script.data()));
                if (jobPosting != null) {
                    return truncate(toJobText(jobPosting));
                }
            } catch (IOException | IllegalArgumentException ignored) {
                // A malformed JSON-LD block should not prevent visible-content extraction.
            }
        }
        return "";
    }

    private JsonNode findJobPosting(JsonNode node) {
        if (node == null) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode item : node) {
                JsonNode jobPosting = findJobPosting(item);
                if (jobPosting != null) {
                    return jobPosting;
                }
            }
            return null;
        }
        if (!node.isObject()) {
            return null;
        }
        if (isJobPosting(node.get("@type"))) {
            return node;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            JsonNode jobPosting = findJobPosting(fields.next().getValue());
            if (jobPosting != null) {
                return jobPosting;
            }
        }
        return null;
    }

    private boolean isJobPosting(JsonNode type) {
        if (type == null) {
            return false;
        }
        if (type.isTextual()) {
            return JOB_POSTING_TYPES.contains(type.asText());
        }
        if (type.isArray()) {
            for (JsonNode item : type) {
                if (isJobPosting(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String toJobText(JsonNode jobPosting) {
        StringBuilder text = new StringBuilder();
        append(text, "Job title", jobPosting.path("title").asText());
        append(text, "Company", jobPosting.path("hiringOrganization").path("name").asText());
        append(text, "Location", jobPosting.path("jobLocation").toString());
        append(text, "Employment type", jobPosting.path("employmentType").toString());
        append(text, "Description", Jsoup.parseBodyFragment(jobPosting.path("description").asText()).text());
        return text.toString();
    }

    private void append(StringBuilder text, String label, String value) {
        if (value != null && !value.isBlank() && !"{}".equals(value) && !"[]".equals(value)) {
            text.append(label).append(": ").append(value).append('\n');
        }
    }

    private URI validateUrl(String jobUrl) {
        if (jobUrl == null || jobUrl.isBlank()) {
            throw new BadRequestException("A job URL is required.");
        }
        try {
            URI uri = URI.create(jobUrl.trim());
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new BadRequestException("The job URL must use HTTP or HTTPS.");
            }
            if (uri.getHost() == null || "localhost".equalsIgnoreCase(uri.getHost())) {
                throw new BadRequestException("The job URL must point to a public website.");
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("The job URL is invalid.");
        }
    }

    private String truncate(String content) {
        return content.length() <= MAX_JOB_CONTENT_LENGTH ? content : content.substring(0, MAX_JOB_CONTENT_LENGTH);
    }

    public static class JobPageUnavailableException extends RuntimeException {
        public JobPageUnavailableException(String message) {
            super(message);
        }

        public JobPageUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
