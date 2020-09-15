package com.shariful.hello.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public abstract class APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAxoi38Pk:APA91bEqOVM_kxBZ_q8zIJVD8ZmNTCKpQzLsZRLrU5xAd2mzU89p6ucPlIevgmj0N2L5WDhC1_OSqSQTdF9DoWUcDRizJCc97Jhh1PyTA31Vo-yZg_O1OuZNOIo2AkJFrAgGggZFLCEY"

    })
    @POST("fcm/send")
    abstract Call<Response> sendNotification(@Body Sender sender) ;




}
