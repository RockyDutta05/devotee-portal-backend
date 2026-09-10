# Devotee Portal Backend

## Load Testing (k6)

This repository includes a k6 load test script (`k6-load-test.js`) to verify the performance and stability of critical endpoints under load.

### Requirements

- Install [k6](https://k6.io/docs/get-started/installation/)

### Running the Test

Run the test against a **staging or development** environment. **Do not run this against production.**

```bash
k6 run --env BASE_URL=https://your-staging-backend.url \
       --env LOGIN_EMAIL=your-test-user@example.com \
       --env LOGIN_PASSWORD=your-test-password \
       k6-load-test.js
```

### Metrics Tracked
- **HTTP Error Rate**: Must stay below 1%
- **Endpoint p95 Latency**: Must stay below 1000ms for:
  - `GET /api/resumes/browse`
  - `GET /api/jobposts`
  - `POST /api/auth/login`
  - `GET /api/referral/willing-referrers`
