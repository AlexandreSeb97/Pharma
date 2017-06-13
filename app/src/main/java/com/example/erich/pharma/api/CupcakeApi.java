package com.example.erich.pharma.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by erich on 6/10/2017.
 */

public interface CupcakeApi {

    @GET("api/item/")
    Call<CupcakeResponse[]> getCupcakesList(@Query("format") String format);
}
