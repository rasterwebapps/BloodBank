package com.bloodbank.documentservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class LocalStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    @Override
    public String uploadFile(String bucket, String objectName, byte[] content, String contentType) {
        log.info("DEV: Uploading file to bucket={}, object={}, size={}, contentType={}",
                bucket, objectName, content != null ? content.length : 0, contentType);
        return bucket + "/" + objectName;
    }

    @Override
    public byte[] downloadFile(String bucket, String objectName) {
        log.info("DEV: Downloading file from bucket={}, object={}", bucket, objectName);
        return new byte[0];
    }

    @Override
    public void deleteFile(String bucket, String objectName) {
        log.info("DEV: Deleting file from bucket={}, object={}", bucket, objectName);
    }
}
