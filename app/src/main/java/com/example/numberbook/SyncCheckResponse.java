package com.example.numberbook;

import java.util.List;

public class SyncCheckResponse {
    private List<Contact> missing_on_app;

    public List<Contact> getMissingOnApp() {
        return missing_on_app;
    }
}