package com.bloodbank.documentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String getEndpoint() { return endpoint; }

    public String getAccessKey() { return accessKey; }

    public String getSecretKey() { return secretKey; }

    public String getBucketName() { return bucketName; }
}
