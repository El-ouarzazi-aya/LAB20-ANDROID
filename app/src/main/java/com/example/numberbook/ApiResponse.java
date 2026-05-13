package com.example.numberbook;

public class ApiResponse {
    private boolean success;
    private String message;
    private boolean duplicate;

    public boolean isSuccess()   { return success; }
    public String getMessage()   { return message; }
    public boolean isDuplicate() { return duplicate; }
}