package com.mindfulai.NetworkRetrofit;


import com.mindfulai.Utils.SPData;

public class ApiUtils {


    public static ApiService getAPIService() {

        return RetrofitClient
                .getClient(SPData.getServerUrl() + "/")
                .create(ApiService.class);
    }

    public static ApiService getKwikAPIService() {

        return RetrofitClient
                .getClient("https://www.kwikapi.com/")
                .create(ApiService.class);
    }

    public static ApiService getHeaderAPIService(String token) {

        return RetrofitClient
                .getClientWithHeader(SPData.getServerUrl() + "/", token)
                .create(ApiService.class);
    }

    public static ApiService getHeaderAPIService() {

        return RetrofitClient
                .getClient(SPData.getServerUrl() + "/")
                .create(ApiService.class);
    }

    public static ApiService getImageAPIService(String token) {

        return RetrofitClient
                .getImageClient(SPData.getServerUrl() + "/", token)
                .create(ApiService.class);
    }


}
