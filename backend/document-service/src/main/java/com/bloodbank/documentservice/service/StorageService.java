package com.bloodbank.documentservice.service;

public interface StorageService {

    String uploadFile(String bucket, String objectName, byte[] content, String contentType);

    byte[] downloadFile(String bucket, String objectName);

    void deleteFile(String bucket, String objectName);
}
