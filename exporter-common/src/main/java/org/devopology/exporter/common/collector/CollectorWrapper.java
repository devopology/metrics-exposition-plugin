/*
 * Copyright 2022 Douglas Hoard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devopology.exporter.common.collector;

import io.prometheus.client.Collector;
import org.devopology.common.precondition.Precondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle the scenario where the JmxExporter is configured with "startDelaySeconds"
 * <p>
 * When "startDelaySeconds" is enabled, collection of ALL metrics fails
 */
public class CollectorWrapper extends Collector implements Collector.Describable {

    private final List<MetricFamilySamples> EMPTY_METRIC_FAMILY_SAMPLES_LIST = new ArrayList<>();

    private Collector collector;

    public CollectorWrapper(Collector collector) {
        Precondition.notNull(collector, "collector is null");

        this.collector = collector;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        try {
            return this.collector.collect();
        } catch (IllegalStateException e) {
            // Fragile code since it relies on the message based on JmxExporter code
            if ("JMXCollector waiting for startDelaySeconds".equals(e.getMessage())) {
                return EMPTY_METRIC_FAMILY_SAMPLES_LIST;
            } else {
                throw e;
            }
        }
    }

    @Override
    public List<MetricFamilySamples> describe() {
        if (this.collector instanceof Describable) {
            return ((Describable) this.collector).describe();
        } else {
            return this.collector.collect();
        }
    }
}
