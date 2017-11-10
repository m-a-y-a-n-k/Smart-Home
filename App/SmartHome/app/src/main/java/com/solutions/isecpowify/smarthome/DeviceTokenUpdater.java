package com.solutions.isecpowify.smarthome;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by mayank on 10/11/17.
 */

public class DeviceTokenUpdater extends FirebaseInstanceIdService {

    private static Map <String,String> queryParams;
    private static String currentToken = null;
    private static OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private static final Callback resCB = new Callback() {
        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            Log.v(Constants.TAG,e.getMessage());
            call.cancel();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response)
        throws IOException {

            if (response.isSuccessful()){
                Log.v(Constants.TAG, String.valueOf(response.body()));
            }
        }
    };


    @Override
    public void onTokenRefresh() {

        // Get updated InstanceID token.
        currentToken = FirebaseInstanceId.getInstance().getToken();
    }

    private static String getDeviceFCMToken(){
        return currentToken;
    }

    static void sendDetailsToServer(final String otrk){

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(getDeviceFCMToken()==null)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                queryParams = Collections.emptyMap();
                queryParams.put("fcm_token",getDeviceFCMToken());
                queryParams.put("id_otr",otrk);
                get(Constants.DEVICE_REG_SERVER,queryParams,resCB);
            }
        }).start();
    }

    public static void get(String url, Map<String,String> params, Callback responseCallback) {

        try{
            HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();

            if (params != null) {
                for(Map.Entry<String, String> param : params.entrySet()) {
                    httpBuilder.addQueryParameter(param.getKey(),param.getValue());
                }
            }
            Request request = new Request.Builder().url(httpBuilder.build()).build();
            client.newCall(request).enqueue(responseCallback);

        } catch (NullPointerException obj){
            Log.v(Constants.TAG,"Object is null : " + obj.getMessage());
        }
    }
}
