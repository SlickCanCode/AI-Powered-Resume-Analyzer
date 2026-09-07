package com.slickdev.resume_analyzer.service.ai.Gemini;

public enum GeminiModel {

    FLASH_LITE_3_5("gemini-3.5-flash-lite"),
    FLASH_LITE_3_1("gemini-3.1-flash-lite"),
    FLASH_3_8("gemini-3.8-flash"),
    FLASH_3_7("gemini-3.7-flash"),
    FLASH_3_6("gemini-3.6-flash"),
    FLASH_3_5("gemini-3.5-flash"),
    FLASH_2_5("gemini-2.5-flash");

    private final String modelName;

    GeminiModel(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }
}
