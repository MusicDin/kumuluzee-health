/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package com.kumuluz.ee.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Health Registry.
 *
 * @author Marko Škrjanec
 * @since 1.0.0
 */
public class HealthRegistry {

    private static HealthRegistry instance;

    private ConcurrentMap<String, HealthCheck> healthChecks;

    private HealthRegistry() {
        healthChecks = new ConcurrentHashMap<String, HealthCheck>();
    }

    /**
     * Returns singleton HealthRegistry instance
     *
     * @return HealthRegistry instance
     */
    public static HealthRegistry getInstance() {

        if (instance == null) {
            instance = new HealthRegistry();
        }

        return instance;
    }

    /**
     * Adds health check to registry.
     *
     * @param healthCheckName
     * @param healthCheck
     */
    public void register(String healthCheckName, HealthCheck healthCheck) {
        healthChecks.put(healthCheckName, healthCheck);
    }

    /**
     * Removes health check from registry.
     *
     * @param healthCheckName
     */
    public void unregister(String healthCheckName) {
        healthChecks.remove(healthCheckName);
    }

    /**
     * Executes health checks in parallel and returns results.
     *
     * @return list of health check results
     */
    public List<HealthCheckResponse> getResults() {
        return this.healthChecks.values().parallelStream().map(healthCheck -> healthCheck.call()).collect(Collectors
                .toList());
    }
}

