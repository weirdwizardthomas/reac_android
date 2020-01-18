package com.via.letmein.persistence.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.via.letmein.persistence.api.Api;
import com.via.letmein.persistence.api.ApiResponse;
import com.via.letmein.persistence.api.ServiceGenerator;
import com.via.letmein.persistence.api.Session;
import com.via.letmein.persistence.api.request.RegisterJson;
import com.via.letmein.persistence.model.Admin;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SessionRepository {

    /**
     * Single instance of the class.
     */
    private static SessionRepository instance;
    /**
     * Retrieved registration response.
     */
    private final MutableLiveData<ApiResponse> registrationData;
    /**
     * Retrieved login response.
     */
    private final MutableLiveData<ApiResponse> loginData;
    /**
     * Application's session data
     */
    private final Session session;

    private SessionRepository(Session session) {
        this.session = session;
        registrationData = new MutableLiveData<>(new ApiResponse());
        loginData = new MutableLiveData<>(new ApiResponse());
    }

    public static synchronized SessionRepository getInstance(Session session) {
        if (instance == null)
            instance = new SessionRepository(session);
        return instance;
    }

    public static synchronized SessionRepository getInstance(Context context) {
        if (instance == null)
            instance = new SessionRepository(Session.getInstance(context));
        return instance;
    }

    public LiveData<ApiResponse> register(String username, String serialNumber) {
        Api api = ServiceGenerator.getApi(session.getIpAddress());
        Log.d("REQUEST", "before the call");
        Call<ApiResponse> call = api.register(new RegisterJson(username, serialNumber));
        Log.d("REQUEST", "after the call");
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse dummy = response.body();
                    Gson gson = new GsonBuilder().create();
                    Log.d("REQUEST", "handling response");
                    if (dummy.isError()) {
                        dummy.setContent(0);
                    } else {
                        TypeToken<Admin> responseTypeToken = new TypeToken<Admin>() {
                        };
                        Admin content = gson.fromJson(
                                gson.toJson(dummy.getContent()),
                                responseTypeToken.getType());
                        dummy.setContent(content);
                    }

                    registrationData.setValue(dummy);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
            }
        });
        return registrationData;
    }

    public LiveData<ApiResponse> getSessionID(String username, String password) {
        Api api = ServiceGenerator.getApi(session.getIpAddress());
        Call<ApiResponse> call = api.login(username, password);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse dummy = response.body();
                    Gson gson = new GsonBuilder().create();

                    if (dummy.isError()) {
                        dummy.setContent(0);
                    } else {
                        TypeToken<String> responseTypeToken = new TypeToken<String>() {
                        };
                        String content = gson.fromJson(
                                gson.toJson(dummy.getContent()),
                                responseTypeToken.getType());
                        dummy.setContent(content);
                    }

                    loginData.setValue(dummy);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
            }
        });

        return loginData;
    }

    private void refresh(Call<ApiResponse> call, final MutableLiveData<ApiResponse> target) {
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse dummy = response.body();
                    Gson gson = new GsonBuilder().create();

                    if (dummy.isError()) {
                        dummy.setContent(0);
                    } else {
                        TypeToken<String> responseTypeToken = new TypeToken<String>() {
                        };
                        String content = gson.fromJson(
                                gson.toJson(dummy.getContent()),
                                responseTypeToken.getType());
                        dummy.setContent(content);
                    }

                    target.setValue(dummy);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
            }
        });


    }

    public String getUsername() {
        return session.getUsername();
    }

    public String getPassword() {
        return session.getPassword();
    }

    public String getSessionID() {
        return session.getSessionId();
    }

    public String getIpAddress() {
        return session.getIpAddress();
    }

    public int getUserID() {
        return session.getUserID();
    }

    public void setUserID(int userID) {
        session.setID(userID);
    }

    public void setIpAddress(String ipAddress) {
        session.setIPAddress(ipAddress);
    }

    public void setPassword(String password) {
        session.setPassword(password);
    }

    public void setRegistered() {
        session.setRegistered(true);
    }

    public void setSessionID(String sessionID) {
        session.setSessionID(sessionID);
    }

    public void setUsername(String username) {
        session.setUsername(username);
    }


    public void wipeSession() {
        session.wipeSession();
    }

}
