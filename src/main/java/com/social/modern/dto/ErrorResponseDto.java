package com.social.modern.dto;

public class ErrorResponseDto {

    private String error;

    public ErrorResponseDto() {
    }

    public ErrorResponseDto(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
