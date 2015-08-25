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

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nullable;

import org.apache.http.client.HttpClient;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

import brooklyn.entity.Effector;
import brooklyn.entity.basic.AbstractEntity;
import brooklyn.event.feed.http.HttpFeed;
import brooklyn.event.feed.http.HttpPollConfig;
import brooklyn.event.feed.http.HttpValueFunctions;
import brooklyn.util.exceptions.Exceptions;
import brooklyn.util.http.HttpTool;
import brooklyn.util.http.HttpToolResponse;
import brooklyn.util.time.Duration;

public class DeviceEntityImpl extends AbstractEntity implements DeviceEntity {

    private HttpFeed feed;
    private String host;
    private String port;

    @Override
    public void init() {
        super.init();

        Function<HttpToolResponse, String> f = HttpValueFunctions.stringContentsFunction();
        Function<String, Double> g = new Function<String, Double>() {
            @Nullable
            @Override
            public Double apply(String input) {
                if (input == null) return null;
                try {
                    return Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };
        Function<HttpToolResponse, Double> composed = Functions.compose(g, f);

        host = checkNotNull(getConfig(HOST), "Config key 'host' is required");
        port = checkNotNull(getConfig(PORT), "Config key 'port' is required");

        feed = HttpFeed.builder()
                .entity(this)
                .period(Duration.ONE_SECOND)
                .baseUri(String.format("http://%s:%s/temperature", host, port))
                .poll(new HttpPollConfig<>(TEMPERATURE)
                        .onSuccess(composed))
                .build();

        final Effector onEffector = checkNotNull(getEffector("relay1On"), "relay1On");
        final Effector offEffector = checkNotNull(getEffector("relay1Off"), "relay1Off");
    }

    @Override
    public void relay1Off() {
        setRelay(1, false);
    }

    @Override
    public void relay1On() {
        setRelay(1, true);
    }

    @Override
    public void relay2Off() {
        setRelay(2, false);
    }

    @Override
    public void relay2On() {
        setRelay(2, true);
    }

    private void setRelay(int relay, boolean state) {
        String uri = String.format("http://%s:%s/relay/%d/%s", host, port, relay, state ? "on" : "off");
        URI uri1 = null;
        try {
            uri1 = new URI(uri);
        } catch (URISyntaxException e) {
            throw Exceptions.propagate(e);
        }
        HttpClient httpClient = HttpTool.httpClientBuilder().build();
        HttpTool.httpPost(httpClient, uri1, ImmutableMap.<String, String>of(), new byte[]{});
    }

}
