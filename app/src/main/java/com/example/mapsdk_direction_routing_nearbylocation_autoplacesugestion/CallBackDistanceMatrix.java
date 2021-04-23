package com.example.mapsdk_direction_routing_nearbylocation_autoplacesugestion;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CallBackDistanceMatrix {
    @GET("maps/api/distancematrix/json?")
    Call<JsonObject> callDistance(@Query("origins") String type,
                                  @Query("destinations") String location,
                                  @Query("key") String apiKey);

}
