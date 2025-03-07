package com.example.cfeprjct.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService {
    @POST("api/password/reset")
    Call<Void> resetPassword(@Body ResetPasswordRequest request);
}
