package com.example.metricbatch.config;

import com.newrelic.telemetry.micrometer.NewRelicRegistry;
import com.newrelic.telemetry.micrometer.NewRelicRegistryConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class NewRelicConfig implements NewRelicRegistryConfig {

    private final NewRelicProperties newRelicProperties;

    public NewRelicConfig(NewRelicProperties newRelicProperties) {
        this.newRelicProperties = newRelicProperties;
    }

    @Override
    public String get(String key) {
        if ("step".equals(key)) {
            return newRelicProperties.getStep();
        }
        return null;
    }

    @Override
    public String apiKey() {
        return newRelicProperties.getApiKey();
    }

    @Override
    public String serviceName() {
        return "Joshua's test";
    }

    @Override
    public boolean enabled() {
        return newRelicProperties.isEnabled();
    }

    @Bean
    public MeterRegistry meterRegistry(NewRelicRegistryConfig config) {
        NewRelicRegistry newRelicRegistry = NewRelicRegistry.builder(config).build();
        newRelicRegistry.start(new NamedThreadFactory("newrelic.micrometer.registry"));
        return newRelicRegistry;
    }
}
