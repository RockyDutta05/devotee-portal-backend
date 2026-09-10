/**
 * Devotee Portal — k6 Load Test Script
 * Phase 5 / Step 3
 *
 * Usage:
 *   k6 run --env BASE_URL=https://your-staging-backend.onrender.com \
 *           --env LOGIN_EMAIL=testuser@example.com \
 *           --env LOGIN_PASSWORD=yourpassword \
 *           k6-load-test.js
 *
 * ⚠️  Run ONLY against the staging/Render preview deployment, NEVER against production.
 */

import http from 'k6/http';
import { sleep, check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// ─── Configuration ────────────────────────────────────────────────────────────

const BASE_URL = __ENV.BASE_URL || 'https://your-staging-backend.onrender.com';
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || 'testuser@example.com';
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || 'password';

// ─── Custom Metrics ──────────────────────────────────────────────────────────

const errorRate = new Rate('http_error_rate');
const browseResumesTrend = new Trend('browse_resumes_duration', true);
const jobpostsTrend      = new Trend('jobposts_duration',       true);
const loginTrend         = new Trend('login_duration',          true);
const referrersTrend     = new Trend('referrers_duration',      true);

// ─── Options ─────────────────────────────────────────────────────────────────

export const options = {
  scenarios: {
    browse_resumes: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 20 }, // ramp up to 20 VUs in 10 s
        { duration: '1m',  target: 20 }, // hold 20 VUs for 1 minute
        { duration: '5s',  target: 0  }, // ramp down
      ],
      exec: 'browseResumes',
      tags: { scenario: 'browse_resumes' },
    },
    jobposts: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 20 },
        { duration: '1m',  target: 20 },
        { duration: '5s',  target: 0  },
      ],
      exec: 'getJobposts',
      tags: { scenario: 'jobposts' },
    },
    login_flow: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 20 },
        { duration: '1m',  target: 20 },
        { duration: '5s',  target: 0  },
      ],
      exec: 'loginFlow',
      tags: { scenario: 'login_flow' },
    },
    willing_referrers: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 20 },
        { duration: '1m',  target: 20 },
        { duration: '5s',  target: 0  },
      ],
      exec: 'getWillingReferrers',
      tags: { scenario: 'willing_referrers' },
    },
  },
  thresholds: {
    // All scenarios should keep p95 under 1000 ms
    'browse_resumes_duration{p95}': ['p(95)<1000'],
    'jobposts_duration{p95}':       ['p(95)<1000'],
    'login_duration{p95}':          ['p(95)<1000'],
    'referrers_duration{p95}':      ['p(95)<1000'],
    // Overall HTTP error rate should stay below 1%
    'http_error_rate':              ['rate<0.01'],
  },
};

// ─── Auth helper (runs once per VU on setup) ─────────────────────────────────

let authToken = null;

function getAuthToken() {
  if (authToken) return authToken;
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  const ok = check(res, { 'login 200': (r) => r.status === 200 });
  if (ok) {
    authToken = res.json('token');
  }
  return authToken;
}

// ─── Scenario functions ───────────────────────────────────────────────────────

export function browseResumes() {
  const token = getAuthToken();
  const headers = token
    ? { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
    : { 'Content-Type': 'application/json' };

  const res = http.get(`${BASE_URL}/api/resumes/browse?page=0&size=20`, { headers });
  browseResumesTrend.add(res.timings.duration);
  const ok = check(res, {
    'browse resumes 200': (r) => r.status === 200,
  });
  errorRate.add(!ok);
  sleep(1);
}

export function getJobposts() {
  const token = getAuthToken();
  const headers = token
    ? { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
    : { 'Content-Type': 'application/json' };

  const res = http.get(`${BASE_URL}/api/jobposts?page=0&size=20`, { headers });
  jobpostsTrend.add(res.timings.duration);
  const ok = check(res, {
    'jobposts 200': (r) => r.status === 200,
  });
  errorRate.add(!ok);
  sleep(1);
}

export function loginFlow() {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  loginTrend.add(res.timings.duration);
  const ok = check(res, {
    'login 200': (r) => r.status === 200,
    'token present': (r) => r.json('token') !== null,
  });
  errorRate.add(!ok);
  sleep(2); // give a bit more breathing room for POST requests
}

export function getWillingReferrers() {
  const token = getAuthToken();
  const headers = token
    ? { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
    : { 'Content-Type': 'application/json' };

  const res = http.get(`${BASE_URL}/api/referral/willing-referrers`, { headers });
  referrersTrend.add(res.timings.duration);
  const ok = check(res, {
    'referrers 200': (r) => r.status === 200,
  });
  errorRate.add(!ok);
  sleep(1);
}
