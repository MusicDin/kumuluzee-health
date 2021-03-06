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
package com.kumuluz.ee.health.checks;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.health.annotations.BuiltInHealthCheck;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rabbit MQ health check.
 *
 * @author Marko Škrjanec
 * @since 1.0.0
 */
@ApplicationScoped
@BuiltInHealthCheck
public class RabbitHealthCheck extends KumuluzHealthCheck implements HealthCheck {

    private static final Logger LOG = Logger.getLogger(RabbitHealthCheck.class.getName());

    // Default rabbit MQ connection url
    private static final String DEFAULT_RABBIT_URL = "amqp://guest:guest@localhost:5672?connection_timeout=2000";

    @Override
    public HealthCheckResponse call() {
        String connectionUrl = ConfigurationUtil.getInstance()
                .get(name() + ".connection-url")
                .orElse(DEFAULT_RABBIT_URL);

        Connection connection = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(connectionUrl);
            connection = factory.newConnection();
            return HealthCheckResponse.up(RabbitHealthCheck.class.getSimpleName());
        } catch (Exception exception) {
            LOG.log(Level.SEVERE, "An exception occurred when trying to establish connection to RabbitMQ.", exception);
            return HealthCheckResponse.down(RabbitHealthCheck.class.getSimpleName());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception exception) {
                    LOG.log(Level.SEVERE, "An exception occurred when trying to close connection to RabbitMQ.",
                            exception);
                }
            }
        }
    }

    @Override
    public String name() {
        return kumuluzBaseHealthConfigPath + "rabbit-health-check";
    }

    @Override
    public boolean initSuccess() {
        try {
            Class.forName("com.rabbitmq.client.Connection");
            return true;
        } catch (ClassNotFoundException e) {
            LOG.severe("The required amqp-client library appears to be missing.");
            return false;
        }
    }
}
