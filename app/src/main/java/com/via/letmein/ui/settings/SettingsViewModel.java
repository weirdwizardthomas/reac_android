package com.via.letmein.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.via.letmein.persistence.api.ApiResponse;
import com.via.letmein.persistence.repository.SessionRepository;

public class SettingsViewModel extends AndroidViewModel {
    SessionRepository sessionRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        sessionRepository = SessionRepository.getInstance(application);
    }

    public String getIPAddress() {
        return sessionRepository.getIpAddress();
    }

    public LiveData<ApiResponse> resetAll() {
        return sessionRepository.resetAll();
    }

    public void resetLocalSession() {
        sessionRepository.resetLocalSession();
    }
}
