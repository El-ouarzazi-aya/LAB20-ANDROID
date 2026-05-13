package com.example.numberbook;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ContactApi {

    @POST("insertContact.php")
    Call<ApiResponse> insertContact(@Body Contact contact);

    @GET("getAllContacts.php")
    Call<List<Contact>> getAllContacts();

    @GET("searchContact.php")
    Call<List<Contact>> searchContacts(@Query("keyword") String keyword);

    @POST("deleteContact.php")
    Call<ApiResponse> deleteContact(@Body Map<String, Integer> body);

    @POST("syncCheck.php")
    Call<SyncCheckResponse> syncCheck(@Body Map<String, List<String>> body);
}