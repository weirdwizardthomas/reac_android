package com.via.letmein.persistence.repository;

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
import com.via.letmein.persistence.api.request.BiometricJson;
import com.via.letmein.persistence.api.request.CreateMemberJson;
import com.via.letmein.persistence.model.HouseholdMember;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for Household members from the server.
 *
 * @author Tomas Koristka: 291129@via.dk
 */
public class HouseholdMemberRepository {

    /**
     * Single instance of the class.
     */
    private static HouseholdMemberRepository instance;
    /**
     * Retrieved memberListLiveData.
     */
    private final MutableLiveData<ApiResponse> memberListLiveData;
    /**
     * Retrieved response from user creation.
     */
    private final MutableLiveData<ApiResponse> createMemberLiveData;
    /**
     * Retrieved repsonse from biometric data's query
     */
    private LiveData<ApiResponse> biometricDataResponse;
    /**
     * API to which requests are sent.
     */
    private final Api api;

    private HouseholdMemberRepository(Session session) {
        api = ServiceGenerator.getApi(session.getIpAddress());
        memberListLiveData = new MutableLiveData<>(new ApiResponse());
        createMemberLiveData = new MutableLiveData<>(new ApiResponse());
        biometricDataResponse = new MutableLiveData<>(new ApiResponse());
    }

    /**
     * Returns the class' single instance.
     *
     * @return Singleton instance of this class.
     */
    public static synchronized HouseholdMemberRepository getInstance(Session session) {
        if (instance == null)
            instance = new HouseholdMemberRepository(session);
        return instance;
    }

    /**
     * Retrieves all received household members
     *
     * @param sessionId ID of the current session
     * @return {@see ApiResponse} with content of list of  if there is no error,
     */
    public MutableLiveData<ApiResponse> getAllHouseholdMembers(String sessionId) {
        refreshUsers(sessionId);
        return memberListLiveData;
    }

    /**
     * Sends an asynchronous call to the server to retrieve users
     *
     * @param sessionId current session ID
     */
    private void refreshUsers(String sessionId) {
        Call<ApiResponse> call = api.getUserList(sessionId);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse dummy = response.body();
                    Gson gson = new GsonBuilder().create();

                    //Check for error & resolve
                    if (dummy.isError())
                        dummy.setContent(0);
                    else {
                        //Convert 'content' to List<HouseholdMember>
                        TypeToken<List<HouseholdMember>> responseTypeToken = new TypeToken<List<HouseholdMember>>() {
                        };
                        List<HouseholdMember> responseList = gson
                                .fromJson(gson.toJson(dummy.getContent()), responseTypeToken.getType());

                        dummy.setContent(responseList);
                    }
                    //save the value
                    memberListLiveData.setValue(dummy);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {

            }
        });

    }

    public LiveData<ApiResponse> createMember(String name, String role, String sessionId) {
        refreshCreateUser(name, role, sessionId);
        return createMemberLiveData;
    }

    private void refreshCreateUser(String name, String role, String sessionId) {
        Call<ApiResponse> call = api.createUser(new CreateMemberJson(name, role, sessionId));
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse dummy = response.body();

                    Gson gson = new GsonBuilder().create();

                    //Check for error & resolve
                    if (dummy.isError())
                        dummy.setContent(0);
                    else {
                        TypeToken<Integer> responseTypeToken = new TypeToken<Integer>() {
                        };
                        Integer responseInteger = gson.fromJson(gson.toJson(dummy.getContent()), responseTypeToken.getType());
                        dummy.setContent(responseInteger);
                    }
                    //save the value
                    createMemberLiveData.setValue(dummy);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {

            }
        });
    }

    public LiveData<ApiResponse> addBiometricData(int userId, String sessionId) {
        Call<ApiResponse> call = api.startBiometricData(new BiometricJson(userId, sessionId));
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse dummy = response.body();

                    Gson gson = new GsonBuilder().create();

                    //Check for error & resolve
                    if (dummy.isError())
                        dummy.setContent(0);
                    else {
                        TypeToken<String> responseTypeToken = new TypeToken<String>() {
                        };
                        String responseString = gson.fromJson(gson.toJson(dummy.getContent()), responseTypeToken.getType());
                        dummy.setContent(responseString);
                    }
                    //save the value
                    createMemberLiveData.setValue(dummy);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {

            }
        });
        return biometricDataResponse;
    }
}
