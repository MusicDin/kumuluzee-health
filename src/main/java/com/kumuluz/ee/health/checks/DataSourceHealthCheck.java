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
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data source health check.
 *
 * @author Marko Škrjanec
 * @since 1.0.0
 */
@ApplicationScoped
@BuiltInHealthCheck
public class DataSourceHealthCheck extends KumuluzHealthCheck implements HealthCheck {

    private static final Logger LOG = Logger.getLogger(DataSourceHealthCheck.class.getName());

    @Override
    public HealthCheckResponse call() {
        Connection connection = null;

        try {
            connection = getConnection();
            return HealthCheckResponse.up(DataSourceHealthCheck.class.getSimpleName());
        } catch (Exception exception) {
            LOG.log(Level.SEVERE, "An exception occurred when trying to establish connection to data source.",
                    exception);
            return HealthCheckResponse.down(DataSourceHealthCheck.class.getSimpleName());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    LOG.log(Level.SEVERE, "An exception occurred when trying to close connection to data source.",
                            exception);
                }
            }
        }
    }

    @Override
    public String name() {
        return kumuluzBaseHealthConfigPath + "data-source-health-check";
    }

    @Override
    public boolean initSuccess() {
        if (!DriverManager.getDrivers().hasMoreElements()) {
            LOG.severe("No database driver library appears to be provided.");
            return false;
        }

        return true;
    }

    /**
     * Helper method for retrieving connection.
     *
     * @return connection
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();

        Optional<String> jndiName = configurationUtil.get(name() + ".jndi-name");
        Optional<Integer> dsSizeOpt = configurationUtil.getListSize("kumuluzee.datasources");

        String connectionUrl = null;
        String username = null;
        String password = null;

        if (jndiName.isPresent() && dsSizeOpt.isPresent()) {
            for (int i = 0; i < dsSizeOpt.get(); i++) {
                String prefix = "kumuluzee.datasources[" + i + "]";
                Optional<String> dsJndiName = configurationUtil.get(prefix + ".jndi-name");

                if (dsJndiName.isPresent() && dsJndiName.get().equals(jndiName.get())) {
                    connectionUrl = configurationUtil.get(prefix + ".connection-url").orElse(null);
                    username = configurationUtil.get(prefix + ".username").orElse(null);
                    password = configurationUtil.get(prefix + ".password").orElse(null);
                    break;
                }
            }
        } else {
            connectionUrl = configurationUtil.get(name() + ".connection-url").orElse(null);
            username = configurationUtil.get(name() + ".username").orElse(null);
            password = configurationUtil.get(name() + ".password").orElse(null);
        }

        return username != null && password != null ? DriverManager.getConnection(connectionUrl, username, password) :
                DriverManager.getConnection(connectionUrl);
    }
}
