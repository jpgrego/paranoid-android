package com.jpgrego.thesisapp.thesisapp.services;

import retrofit2.Call;
import retrofit2.http.POST;

/**
 * Created by jpgrego on 05/01/17.
 */

interface MozillaLocationService {
    String MOZILLA_API_KEY = "test";
    String MOZILLA_LOCATION_SERVICE_URL = "https://location.services.mozilla.com/";

    @POST("v1/geolocate?key=" + MOZILLA_API_KEY)
    Call<MozillaLocationResponse> geolocate();
}
