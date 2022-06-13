package com.example.metricbatch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@SpringBootApplication
public class MetricBatchApplication implements CommandLineRunner {
	private final MeterRegistry meterRegistry;

	@Value("${counter-name}")
	private String COUNTER_NAME;
	@Value("${sleep-ms}")
	private Long SLEEP_MS;

	public MetricBatchApplication(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void run(String[] args) throws Exception {
		Counter counter = meterRegistry.counter(COUNTER_NAME);
		counter.increment();
		Thread.sleep(SLEEP_MS);
	}

	public static void main(String[] args) {
		SpringApplication.run(MetricBatchApplication.class, args);
	}
}

