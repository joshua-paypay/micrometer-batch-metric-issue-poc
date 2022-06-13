# PoC of micrometer issue

This is a PoC of the issue [micrometer#1882](https://github.com/micrometer-metrics/micrometer/issues/1882) and another related (might be the same?) issue that `PushMeterRegistry` sends the same metrics twice on shutdown because [this line](https://github.com/micrometer-metrics/micrometer/blob/main/micrometer-core/src/main/java/io/micrometer/core/instrument/push/PushMeterRegistry.java#L96) publishes metrics but it might be in the same step to the last push. So the metric was pushed twice.

I can reproduce this on New Relic. 

## Run

1. Set properties in `application.properties`.
2. `./gradlew bootRun`

## Reproduce 1

1. Set `sleep-ms` to 0
2. Run the batch
3. check metric. I can see only one push with count = 0. This is [micrometer#1882](https://github.com/micrometer-metrics/micrometer/issues/1882)

```
June 13, 2022 15:42:45
{"type":"count","count":0.0}
```

DEBUG log
```
2022-06-13 15:42:45.406 DEBUG 9276 --- [       Thread-2] c.n.telemetry.metrics.MetricBatchSender  : Sending a metric batch (number of metrics: 1) to the New Relic metric ingest endpoint)
2022-06-13 15:42:45.406 DEBUG 9276 --- [       Thread-2] c.n.t.m.json.MetricBatchMarshaller       : Generating json for metric batch.
2022-06-13 15:42:47.174 DEBUG 9276 --- [       Thread-2] c.n.telemetry.transport.BatchDataSender  : Response from New Relic ingest API for MetricBatch: code: 202, body: {"requestId":"4b22f2fe-0001-bf8f-8ca3-01815bccf0e2"}
2022-06-13 15:42:47.174 DEBUG 9276 --- [       Thread-2] com.newrelic.telemetry.TelemetryClient   : Telemetry - MetricBatch - sent
```

## Reproduce 2

1. Set `sleep-ms` to 60000 (1 minute)
2. Run the metrics. I can see two identical json objects being pushed to New Relic with timestamps differ 1 minute. This is the new issue I want to report. The second count isn't zeroed.
 
```
June 13, 2022 15:46:00
{"type":"count","count":1.0}
June 13, 2022 15:45:00
{"type":"count","count":1.0}
```

```
2022-06-13 15:45:00.392  INFO 9378 --- [           main] c.e.metricbatch.MetricBatchApplication   : No active profile set, falling back to 1 default profile: "default"
2022-06-13 15:45:00.775  INFO 9378 --- [           main] c.n.telemetry.transport.BatchDataSender  : BatchDataSender configured with endpoint https://metric-api.newrelic.com/metric/v1
2022-06-13 15:45:00.776  INFO 9378 --- [           main] c.n.telemetry.transport.BatchDataSender  : BatchDataSender configured to use insights keys
2022-06-13 15:45:00.805  INFO 9378 --- [           main] c.n.t.micrometer.NewRelicRegistry        : New Relic Registry: Version 0.9.0 is starting
2022-06-13 15:45:00.806  INFO 9378 --- [           main] i.m.c.instrument.push.PushMeterRegistry  : publishing metrics for NewRelicRegistry every 1m
2022-06-13 15:45:00.852  INFO 9378 --- [           main] c.e.metricbatch.MetricBatchApplication   : Started MetricBatchApplication in 0.825 seconds (JVM running for 1.225)
2022-06-13 15:46:00.024 DEBUG 9378 --- [       Thread-2] c.n.telemetry.metrics.MetricBatchSender  : Sending a metric batch (number of metrics: 1) to the New Relic metric ingest endpoint)
2022-06-13 15:46:00.025 DEBUG 9378 --- [       Thread-2] c.n.t.m.json.MetricBatchMarshaller       : Generating json for metric batch.
2022-06-13 15:46:00.869  INFO 9378 --- [ionShutdownHook] com.newrelic.telemetry.TelemetryClient   : Shutting down the TelemetryClient background Executor
2022-06-13 15:46:00.913 DEBUG 9378 --- [       Thread-2] c.n.telemetry.transport.BatchDataSender  : Response from New Relic ingest API for MetricBatch: code: 202, body: {"requestId":"228044fc-0001-ba21-4731-01815bcfe787"}
2022-06-13 15:46:00.914 DEBUG 9378 --- [       Thread-2] com.newrelic.telemetry.TelemetryClient   : Telemetry - MetricBatch - sent
2022-06-13 15:46:00.914 DEBUG 9378 --- [       Thread-2] c.n.telemetry.metrics.MetricBatchSender  : Sending a metric batch (number of metrics: 1) to the New Relic metric ingest endpoint)
2022-06-13 15:46:00.914 DEBUG 9378 --- [       Thread-2] c.n.t.m.json.MetricBatchMarshaller       : Generating json for metric batch.
2022-06-13 15:46:01.170 DEBUG 9378 --- [       Thread-2] c.n.telemetry.transport.BatchDataSender  : Response from New Relic ingest API for MetricBatch: code: 202, body: {"requestId":"aaf441fd-0001-bf1b-3568-01815bcfe88f"}
2022-06-13 15:46:01.171 DEBUG 9378 --- [       Thread-2] com.newrelic.telemetry.TelemetryClient   : Telemetry - MetricBatch - sent
```

Another test in different time

```
June 13, 2022 15:55:00
{"type":"count","count":1.0}
June 13, 2022 15:54:50
{"type":"count","count":1.0}
```

```
// Application Starts
2022-06-13 15:54:50.310  INFO 9830 --- [           main] c.e.metricbatch.MetricBatchApplication   : No active profile set, falling back to 1 default profile: "default"
2022-06-13 15:54:50.682  INFO 9830 --- [           main] c.n.telemetry.transport.BatchDataSender  : BatchDataSender configured with endpoint https://metric-api.newrelic.com/metric/v1
2022-06-13 15:54:50.683  INFO 9830 --- [           main] c.n.telemetry.transport.BatchDataSender  : BatchDataSender configured to use insights keys
2022-06-13 15:54:50.716  INFO 9830 --- [           main] c.n.t.micrometer.NewRelicRegistry        : New Relic Registry: Version 0.9.0 is starting
2022-06-13 15:54:50.716  INFO 9830 --- [           main] i.m.c.instrument.push.PushMeterRegistry  : publishing metrics for NewRelicRegistry every 1m
2022-06-13 15:54:50.763  INFO 9830 --- [           main] c.e.metricbatch.MetricBatchApplication   : Started MetricBatchApplication in 0.812 seconds (JVM running for 1.223)

// first scheduled report is done. 
// This sends data from the start (15:54:50) to 15:55:00.
// So on New Relic we see timestamp 15:54:50

2022-06-13 15:55:00.017 DEBUG 9830 --- [       Thread-2] c.n.telemetry.metrics.MetricBatchSender  : Sending a metric batch (number of metrics: 1) to the New Relic metric ingest endpoint)
2022-06-13 15:55:00.017 DEBUG 9830 --- [       Thread-2] c.n.t.m.json.MetricBatchMarshaller       : Generating json for metric batch.
2022-06-13 15:55:00.882 DEBUG 9830 --- [       Thread-2] c.n.telemetry.transport.BatchDataSender  : Response from New Relic ingest API for MetricBatch: code: 202, body: {"requestId":"b52e7bfe-0001-bfa6-f4af-01815bd82495"}
2022-06-13 15:55:00.883 DEBUG 9830 --- [       Thread-2] com.newrelic.telemetry.TelemetryClient   : Telemetry - MetricBatch - sent

// metric reported on shutdown. This is from last report (15:55:00) to 15:55:50. 
// They are in the same minute
// so the data fetched are the same.

2022-06-13 15:55:50.779 DEBUG 9830 --- [       Thread-2] c.n.telemetry.metrics.MetricBatchSender  : Sending a metric batch (number of metrics: 1) to the New Relic metric ingest endpoint)
2022-06-13 15:55:50.779 DEBUG 9830 --- [       Thread-2] c.n.t.m.json.MetricBatchMarshaller       : Generating json for metric batch.
2022-06-13 15:55:50.779  INFO 9830 --- [ionShutdownHook] com.newrelic.telemetry.TelemetryClient   : Shutting down the TelemetryClient background Executor
2022-06-13 15:55:51.359 DEBUG 9830 --- [       Thread-2] c.n.telemetry.transport.BatchDataSender  : Response from New Relic ingest API for MetricBatch: code: 202, body: {"requestId":"d60497e7-0001-b06e-79b9-01815bd8e9c0"}
2022-06-13 15:55:51.359 DEBUG 9830 --- [       Thread-2] com.newrelic.telemetry.TelemetryClient   : Telemetry - MetricBatch - sent
```


## Reproduce 3

1. Set `sleep-ms` to 120000 (2 minutes)
2. Run the metrics. I can see three json objects being pushed to New Relic. The metrics work as expected.

```
 June 13, 2022 15:26:00
{"type":"count","count":0.0}
June 13, 2022 15:25:00
{"type":"count","count":0.0}
June 13, 2022 15:24:00
{"type":"count","count":1.0}
```

## Potential Fixes
1. We can have a option to not do another `publish()` out of normal schedule on shutdown. Instead to wait until the next metric report on shutdown.
2. When doing `publish()` out of normal schedule, see it as another step (different to normal step). So the count / timer will be zeroed. Don't know if this will affect behaviors in other metrics types.
