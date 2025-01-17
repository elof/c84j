/*
 * DISCLAIMER
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.c8db.internal.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.c8db.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c8db.internal.velocystream.internal.VstConnection;
import com.c8db.internal.velocystream.internal.VstConnectionSync;

/**
 *
 */
public class ConnectionPoolImpl implements ConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPoolImpl.class);

    private final HostDescription host;
    private final int maxConnections;
    private final List<Connection> connections;
    private int current;
    private final ConnectionFactory factory;
    private final Service service;

    public ConnectionPoolImpl(final HostDescription host, final Integer maxConnections,
            final ConnectionFactory factory, Service service) {
        super();
        this.host = host;
        this.maxConnections = maxConnections;
        this.factory = factory;
        this.service = service;
        connections = new ArrayList<Connection>();
        current = 0;
    }

    @Override
    public Connection createConnection(final HostDescription host) {
        return factory.create(host, service);
    }

    @Override
    public synchronized Connection connection() {

        final Connection connection;

        if (connections.size() < maxConnections) {
            connection = createConnection(host);
            connections.add(connection);
            current++;
        } else {
            final int index = (current++) % connections.size();
            connection = connections.get(index);
        }

        if (connection instanceof VstConnectionSync) {
            LOGGER.debug("Return Connection " + ((VstConnection) connection).getConnectionName());
        }

        return connection;
    }

    @Override
    public void close() throws IOException {
        for (final Connection connection : connections) {
            connection.close();
        }
        connections.clear();
    }

    @Override
    public String toString() {
        return "ConnectionPoolImpl [host=" + host + ", maxConnections=" + maxConnections + ", connections="
                + connections.size() + ", current=" + current + ", factory=" + factory.getClass().getSimpleName() + "]";
    }

}
