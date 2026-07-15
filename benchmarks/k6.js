import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const paymentLatency = new Trend('payment_latency', true);
const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8080';

export const options = {
  vus: Number(__ENV.VUS || 32),
  duration: __ENV.DURATION || '10s',
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)', 'count'],
  thresholds: { checks: ['rate==1'], http_req_failed: ['rate==0'], payment_latency: ['p(99)<500'] },
};

function authorizationRequest(key, reference) {
  return {
    method: 'POST',
    url: `${baseUrl}/v1/payments`,
    body: JSON.stringify({ amount_minor: 2590, currency: 'BRL', merchant_reference: reference }),
    params: { headers: { 'Content-Type': 'application/json', 'Idempotency-Key': key } },
  };
}

export function setup() {
  const total = Number(__ENV.WARMUP_REQUESTS || 200);
  for (let offset = 0; offset < total; offset += 20) {
    const batch = [];
    for (let index = offset; index < Math.min(offset + 20, total); index += 1) {
      batch.push(authorizationRequest(`warmup-${Date.now()}-${index}`, `warmup-order-${Date.now()}-${index}`));
    }
    const responses = http.batch(batch);
    const failed = responses.find((response) => response.status !== 201);
    if (failed) {
      throw new Error(`warmup authorization failed: status=${failed.status} body=${failed.body.slice(0, 200)}`);
    }
  }
}

export default function () {
  const unique = `${__VU}-${__ITER}-${Date.now()}`;
  const request = authorizationRequest(`payment-${unique}`, `order-${unique}`);
  const response = http.request(request.method, request.url, request.body, request.params);
  if (response.status === 201) paymentLatency.add(response.timings.duration);
  check(response, { 'payment authorized': (result) => result.status === 201 });
}

export function handleSummary(data) {
  const p99 = data.metrics.payment_latency.values['p(99)'];
  const durationSeconds = Number((__ENV.DURATION || '10s').replace(/s$/, ''));
  const result = {
    project: 'spring-hexagonal-payments',
    metric: 'p99_latency_ms',
    value: Math.round(p99 * 1000) / 1000,
    unit: 'milliseconds',
    timestamp: new Date().toISOString(),
    command: 'docker run --rm spring-hexagonal-payments',
    repeat: Number(__ENV.REPEAT || 1),
    summary: {
      p99_latency_ms: Math.round(p99 * 1000) / 1000,
      throughput_rps: Math.round((data.metrics.iterations.values.count / durationSeconds) * 100) / 100,
      measured_requests: data.metrics.iterations.values.count,
      warmup_requests: Number(__ENV.WARMUP_REQUESTS || 200),
      coverage_percent: Number(__ENV.COVERAGE_PERCENT || 0),
      checks_rate: data.metrics.checks.values.rate,
      http_failure_rate: data.metrics.http_req_failed.values.rate,
    },
    environment: {
      java: '25',
      kotlin: '2.4.10',
      spring_boot: '4.1.0',
      postgresql: '18.4',
      load_tool: 'k6-2.1.0',
    },
  };
  const output = JSON.stringify(result, null, 2) + '\n';
  return { [__ENV.RESULT_PATH || '/tmp/payments-baseline.json']: output, stdout: output };
}
