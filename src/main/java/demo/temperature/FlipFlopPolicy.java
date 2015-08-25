// Copyright 2015 Richard Downer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package demo.temperature;

import static org.python.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.AtomicDouble;

import brooklyn.catalog.Catalog;
import brooklyn.config.ConfigKey;
import brooklyn.entity.Effector;
import brooklyn.entity.EntityType;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.EntityLocal;
import brooklyn.event.AttributeSensor;
import brooklyn.event.SensorEvent;
import brooklyn.event.SensorEventListener;
import brooklyn.event.basic.BasicConfigKey;
import brooklyn.policy.Policy;
import brooklyn.policy.basic.AbstractPolicy;

@Catalog(name = "flip-flop policy", description = "a simple policy which monitors a sensor against a threshold value; " +
        "if the value transitions above the threshold an 'on' effector is invoked; if the value transistions belofe the " +
        "threshold an 'off' effector is invoked.")
public class FlipFlopPolicy extends AbstractPolicy implements Policy {

    public static final ConfigKey<Double> THRESHOLD = BasicConfigKey.builder(Double.class)
            .name("threshold")
            .description("the threshold which causes an effector invocation")
            .defaultValue(30D)
            .reconfigurable(true)
            .build();
    public static final ConfigKey<String> ON_EFFECTOR_NAME = ConfigKeys.newStringConfigKey("onEffectorName", "effector to call when sensor goes above the threshold");
    public static final ConfigKey<String> OFF_EFFECTOR_NAME = ConfigKeys.newStringConfigKey("offEffectorName", "effector to call when sensor goes below the threshold");

    public static final ConfigKey<AttributeSensor<Double>> METRIC = BasicConfigKey.builder(new TypeToken<AttributeSensor<Double>>() {
    })
            .name("metric")
            .build();

    private Boolean state = null;
    private AtomicDouble threshold;

    @Override
    public void init() {
        doInit();
    }

    @Override
    public void rebind() {
        doInit();
    }

    protected void doInit() {
        threshold = new AtomicDouble(checkNotNull(getConfig(THRESHOLD), "THRESHOLD"));
    }

    @Override
    public void setEntity(final EntityLocal entity) {
        super.setEntity(entity);

        EntityType entityType = entity.getEntityType();
        final String onEffectorName = checkNotNull(getConfig(ON_EFFECTOR_NAME), "ON_EFFECTOR_NAME");
        final String offEffectorName = checkNotNull(getConfig(OFF_EFFECTOR_NAME), "OFF_EFFECTOR_NAME");
        final Effector<?> onEffector = checkNotNull(entityType.getEffectorByName(onEffectorName).orNull(), onEffectorName);
        final Effector<?> offEffector = checkNotNull(entityType.getEffectorByName(offEffectorName).orNull(), offEffectorName);
        final AttributeSensor<Double> metric = checkNotNull(getConfig(METRIC), "METRIC");

        entity.subscribe(entity, metric, new ThresholdSensorEventListener(onEffector, offEffector, entity));
    }

    @Override
    protected <T> void doReconfigureConfig(ConfigKey<T> key, T val) {
        if (key.equals(THRESHOLD)) {
            threshold.set(checkNotNull((Double)val, "THRESHOLD"));
        } else {
            throw new UnsupportedOperationException("reconfiguring "+key+" unsupported for "+this);
        }
    }

    private class ThresholdSensorEventListener implements SensorEventListener<Double> {
        private final Effector<?> onEffector;
        private final Effector<?> offEffector;
        private final EntityLocal entity;

        public ThresholdSensorEventListener(Effector<?> onEffector, Effector<?> offEffector, EntityLocal entity) {
            this.onEffector = onEffector;
            this.offEffector = offEffector;
            this.entity = entity;
        }

        @Override
        public void onEvent(SensorEvent<Double> event) {
            Double val = event.getValue();
            if (val == null) return;

            Boolean newState = val > threshold.get() ? Boolean.TRUE : Boolean.FALSE;
            if (newState != state) {
                Effector effector = newState ? onEffector : offEffector;
                entity.invoke(effector, ImmutableMap.<String, Object>of());
                state = newState;
            }
        }
    }
}
