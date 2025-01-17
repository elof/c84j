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

package com.c8db.internal.http;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.c8db.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.c8db.C8DBException;
import com.c8db.internal.net.AccessType;
import com.c8db.internal.net.C8DBRedirectException;
import com.c8db.internal.net.Host;
import com.c8db.internal.net.HostDescription;
import com.c8db.internal.net.HostHandle;
import com.c8db.internal.net.HostHandler;
import com.c8db.internal.util.HostUtils;
import com.c8db.internal.util.RequestUtils;
import com.c8db.util.C8Serialization;
import com.c8db.velocystream.Request;
import com.c8db.velocystream.Response;

/**
 *
 */
public class HttpCommunication implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpCommunication.class);

    public static class Builder {

        private final Map<Service, HostHandler> hostHandlerMatrix;

        public Builder(final Map<Service, HostHandler> hostHandlerMatrix) {
            super();
            this.hostHandlerMatrix = hostHandlerMatrix;
        }

        public Builder(final Builder builder) {
            this(builder.hostHandlerMatrix);
        }

        public HttpCommunication build(final C8Serialization util) {
            return new HttpCommunication(hostHandlerMatrix);
        }
    }

    private final Map<Service, HostHandler> hostHandlerMatrix;

    private HttpCommunication(final Map<Service, HostHandler> hostHandlerMatrix) {
        super();
        this.hostHandlerMatrix = hostHandlerMatrix;
    }

    @Override
    public void close() throws IOException {
        for (HostHandler hostHandler : hostHandlerMatrix.values()) {
            hostHandler.close();
        }
    }

    private AtomicInteger c = new AtomicInteger(0);

    public Response execute(final Request request, final HostHandle hostHandle, Service service) throws C8DBException, IOException {
        final AccessType accessType = RequestUtils.determineAccessType(request);
        HostHandler hostHandler = hostHandlerMatrix.get(service);
        Host host = hostHandler.get(hostHandle, accessType);
        try {
            while (true) {
                try {
                    final HttpConnection connection = (HttpConnection) host.connection();
                    final Response response = connection.execute(request);
                    hostHandler.success();
                    hostHandler.confirm();
                    return response;
                } catch (final SocketException se) {
                    hostHandler.fail();
                    if (hostHandle != null && hostHandle.getHost() != null) {
                        hostHandle.setHost(null);
                    }
                    final Host failedHost = host;
                    host = hostHandler.get(hostHandle, accessType);
                    if (host != null) {
                        LOGGER.warn(String.format("Could not connect to %s. Try connecting to %s",
                                failedHost.getDescription(), host.getDescription()));
                    } else {
                        throw se;
                    }
                }
            }
        } catch (final C8DBException e) {
            if (e instanceof C8DBRedirectException) {
                final String location = C8DBRedirectException.class.cast(e).getLocation();
                final HostDescription redirectHost = HostUtils.createFromLocation(location);
                hostHandler.closeCurrentOnError();
                hostHandler.fail();
                return execute(request, new HostHandle().setHost(redirectHost), service);
            } else {
                throw e;
            }
        }
    }

}
