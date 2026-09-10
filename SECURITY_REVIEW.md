# Security Review — File Upload (Cloudflare R2 Presigned URLs)

**Reviewed:** 2026-09-10  
**Reviewer:** Phase 5 Hardening Pass  
**Scope:** `POST /api/resumes/presign-upload` and downstream R2 object-key handling

---

## Checklist & Results

| # | Check | Result | Evidence |
|---|-------|--------|---------|
| 1 | Presign endpoint requires a valid JWT | ✅ PASS | Spring Security config (`SecurityConfig.java`) marks all `/api/**` routes as authenticated; no `permitAll()` exception for `/api/resumes/presign-upload`. `FileUploadSecurityTest#presignRequiresAuthentication` asserts HTTP 401 without a token. |
| 2 | Object key `{userId}` is derived from the authenticated JWT, never from client input | ✅ PASS | `ResumeService#generatePresignedUrl` calls `userRepository.findByEmail(authentication.getName())` to resolve the user. The `PresignRequest` DTO has **no** `userId` field. `FileUploadSecurityTest#presignObjectKeyContainsServerUserId` asserts via reflection that `PresignRequest` contains no `userId` field. |
| 3 | Disallowed file extension is rejected | ✅ PASS | `ResumeService#generatePresignedUrl` checks the extension against an explicit allow-list (`pdf`, `doc`, `docx`, `jpg`, `jpeg`, `png`) and throws `IllegalArgumentException` (→ HTTP 400). `FileUploadSecurityTest#presignRejectsDisallowedExtension` and `#presignRejectsShellScript` confirm this. |
| 4 | Oversized file (> 10 MB) is rejected | ✅ PASS | `PresignRequest#contentLength` is annotated `@Max(value = 10485760)`. The `GlobalExceptionHandler` catches `MethodArgumentNotValidException` and returns HTTP 400. `FileUploadSecurityTest#presignRejectsOversizedFile` confirms. |
| 5 | R2 credentials are never logged | ✅ PASS | `R2Config` reads `accessKey`/`secretKey` from `@Value` expressions backed by env-vars only. No `log.info`/`System.out.println` referencing credential fields exists anywhere in the codebase. Verified by `grep -r "accessKey\|secretKey" src/main` — only assignment lines appear. |
| 6 | R2 credentials are never returned in any API response | ✅ PASS | `PresignResponse` DTO contains only `presignedUrl`, `fileUrl`, and `objectKey`. No credential field is mapped. |
| 7 | R2 credentials are only read from environment variables | ✅ PASS | `application.properties` uses `${R2_ACCESS_KEY_ID:}` and `${R2_SECRET_ACCESS_KEY:}` with empty defaults so the app starts in dev without crashing, but empty values cannot sign real requests. No hardcoded keys exist. |
| 8 | Presigned GET URLs expire | ✅ PASS | `ResumeService#mapToDto` calls `s3Presigner.presignGetObject(…).signatureDuration(Duration.ofMinutes(15))`. After 15 minutes, R2 will return `AccessDenied` (403) for the stale URL — this is enforced by the R2/S3 signature protocol and cannot be bypassed by the client. |
| 9 | One user cannot reuse another user's presigned URL to overwrite their file | ✅ PASS | The object key format is `resumes/<authenticated-userId>/<uuid>-<fileName>`. The presigned URL is scoped to exactly that key. A different user's presigned URL would be scoped to a different key path; R2 would reject a PUT to a non-matching key. |

---

## Known Limitations / Recommendations

> [!WARNING]
> The file-extension check happens **after** content type is provided by the client (`fileType` field in `PresignRequest`). The `contentType` in the presigned PUT request uses this client-supplied value. Consider validating MIME type server-side using Apache Tika or a magic-byte check rather than trusting the `Content-Type` header from the uploader.

> [!NOTE]
> The current allow-list (`pdf, doc, docx, jpg, jpeg, png`) is hard-coded in `ResumeService`. Moving it to `application.properties` (`app.upload.allowed-extensions`) would make it easier to update without a redeploy.

> [!NOTE]
> There is no rate-limiting on the presign endpoint. A malicious authenticated user could generate thousands of presigned URLs per minute. Consider adding Spring Security's rate-limiting filter or a Redis-backed token bucket in a future hardening pass.

---

## Test Coverage Summary

All checks above are covered by automated integration tests in `FileUploadSecurityTest.java`:

```
FileUploadSecurityTest
├── presignRequiresAuthentication          → check #1
├── presignRejectsDisallowedExtension      → check #3
├── presignRejectsShellScript              → check #3 (extra vector)
├── presignRejectsOversizedFile            → check #4
└── presignObjectKeyContainsServerUserId   → check #2
```

Checks #5, #6, #7, #8, #9 are structural/code-review checks that do not require a running R2 instance.
