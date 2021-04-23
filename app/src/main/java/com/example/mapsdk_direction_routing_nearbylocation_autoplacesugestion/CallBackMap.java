package com.example.mapsdk_direction_routing_nearbylocation_autoplacesugestion;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CallBackMap  {

    @GET("maps/api/place/nearbysearch/json?radius=5000&keyword=cruise")
    Call<JsonObject> callNearbyPlaces(@Query("type") String type,
                                      @Query("location") String location,
                                      @Query("key") String apiKey);

}
