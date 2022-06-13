package com.example.metricbatch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "management.metrics.export.newrelic")
public class NewRelicProperties {
    private boolean enabled;
    private String apiKey;
    private String accountId;
    private String step;
}
