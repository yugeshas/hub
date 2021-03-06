package com.flightstats.hub.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.flightstats.hub.config.properties.MetricsProperties;
import javax.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import java.util.concurrent.TimeUnit;

@Singleton
public class MetricRegistryProvider implements Provider<MetricRegistry> {
    private MetricsProperties metricsProperties;

    @Inject
    public MetricRegistryProvider(MetricsProperties metricsProperties) {
        this.metricsProperties = metricsProperties;
    }

    public MetricRegistry get() {
        MetricRegistry metricsRegistry = new MetricRegistry();
        int intervalSeconds = metricsProperties.getReportingIntervalInSeconds();
        metricsRegistry.register("gc", new GarbageCollectorMetricSet());
        metricsRegistry.register("thread", new CachedThreadStatesGaugeSet(intervalSeconds, TimeUnit.SECONDS));
        metricsRegistry.register("memory", new MemoryUsageGaugeSet());
        return metricsRegistry;
    }
}
