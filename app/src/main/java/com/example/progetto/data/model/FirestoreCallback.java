package com.example.progetto.data.model;

public interface FirestoreCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}