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

import brooklyn.catalog.Catalog;
import brooklyn.config.ConfigKey;
import brooklyn.entity.Entity;
import brooklyn.entity.annotation.Effector;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.Sensors;

@ImplementedBy(DeviceEntityImpl.class)
@Catalog(name = "Temperature Sensor/Control Device", description = "Connects to the HTTP API provided by the " +
        "temperature sensor and control device, providing Brooklyn sensors to read state, and effectors to change the " +
        "controls.")
public interface DeviceEntity extends Entity {

    public static final ConfigKey<String> HOST = ConfigKeys.newStringConfigKey("host", "Hostname or IP address of the temperature sensor");
    public static final ConfigKey<String> PORT = ConfigKeys.newStringConfigKey("port", "Port number of the temperature sensor");
    public static final AttributeSensor<Double> TEMPERATURE = Sensors.newDoubleSensor("temperature", "Reading from the temperature sensor in degrees celsius");
    
    @Effector(description = "Switch relay 1 off")
    public void relay1Off();
    
    @Effector(description = "Switch relay 1 on")
    public void relay1On();
    
    @Effector(description = "Switch relay 2 off")
    public void relay2Off();
    
    @Effector(description = "Switch relay 2 on")
    public void relay2On();
    
}
