import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CORSRule;
import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;

import java.net.URI;
import java.util.Arrays;

public class CorsConfigurator {
    public static void main(String[] args) {
        // Read credentials from env (since we'll run this via terminal we can pass them or read them)
        String accessKey = System.getenv("R2_ACCESS_KEY_ID");
        String secretKey = System.getenv("R2_SECRET_ACCESS_KEY");
        String endpointUrl = System.getenv("R2_ENDPOINT");
        String bucketName = System.getenv("R2_BUCKET_NAME");

        if (accessKey == null || secretKey == null || endpointUrl == null || bucketName == null) {
            System.err.println("Missing required environment variables!");
            System.exit(1);
        }

        S3Client s3Client = S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpointUrl))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        CORSRule corsRule = CORSRule.builder()
                .allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", "https://localhost:5173")
                .allowedMethods("PUT", "GET", "POST", "HEAD")
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

        try {
            s3Client.putBucketCors(putBucketCorsRequest);
            System.out.println("Successfully updated CORS policy for bucket: " + bucketName);
        } catch (Exception e) {
            System.err.println("Failed to update CORS policy: " + e.getMessage());
        }
    }
}
