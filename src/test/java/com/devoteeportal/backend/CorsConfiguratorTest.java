package com.devoteeportal.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;

import java.net.URI;
import java.util.Arrays;

@SpringBootTest
public class CorsConfiguratorTest {

    @Value("${app.r2.access-key}")
    private String accessKey;

    @Value("${app.r2.secret-key}")
    private String secretKey;

    @Value("${app.r2.endpoint-url}")
    private String endpointUrl;

    @Value("${app.r2.bucket-name}")
    private String bucketName;

    @Test
    public void configureCors() {
        S3Client s3Client = S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        CORSRule corsRule = CORSRule.builder()
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "https://localhost:5173")
                .allowedMethods("PUT", "GET", "POST", "HEAD", "DELETE")
                .allowedHeaders("*")
                .maxAgeSeconds(3000)
                .build();

        CORSConfiguration corsConfiguration = CORSConfiguration.builder()
                .corsRules(Arrays.asList(corsRule))
                .build();

        PutBucketCorsRequest putBucketCorsRequest = PutBucketCorsRequest.builder()
                .bucket(bucketName)
                .corsConfiguration(corsConfiguration)
                .build();

        s3Client.putBucketCors(putBucketCorsRequest);
        System.out.println("==========================================");
        System.out.println("SUCCESSFULLY UPDATED CORS FOR BUCKET: " + bucketName);
        System.out.println("==========================================");
    }
}
