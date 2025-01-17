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

package com.c8db.internal.velocystream;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;

import com.c8db.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackParserException;
import com.c8db.C8DBException;
import com.c8db.internal.C8Defaults;
import com.c8db.internal.net.AccessType;
import com.c8db.internal.net.C8DBRedirectException;
import com.c8db.internal.net.Host;
import com.c8db.internal.net.HostDescription;
import com.c8db.internal.net.HostHandle;
import com.c8db.internal.net.HostHandler;
import com.c8db.internal.util.HostUtils;
import com.c8db.internal.util.RequestUtils;
import com.c8db.internal.util.ResponseUtils;
import com.c8db.internal.velocystream.internal.Chunk;
import com.c8db.internal.velocystream.internal.Message;
import com.c8db.internal.velocystream.internal.VstConnection;
import com.c8db.util.C8Serialization;
import com.c8db.velocystream.Request;
import com.c8db.velocystream.Response;

/**
 *
 */
public abstract class VstCommunication<R, C extends VstConnection> implements Closeable {

    protected static final String ENCRYPTION_PLAIN = "plain";
    private static final Logger LOGGER = LoggerFactory.getLogger(VstCommunication.class);

    protected static final AtomicLong mId = new AtomicLong(0L);
    protected final C8Serialization util;

    protected final String user;
    protected final String password;

    protected final Integer chunksize;
    private final Map<Service, HostHandler> hostHandlerMatrix;

    protected VstCommunication(final Integer timeout, final String user, final String password, final Boolean useSsl,
            final SSLContext sslContext, final C8Serialization util, final Integer chunksize,
            final Map<Service, HostHandler> hostHandlerMatrix) {
        this.user = user;
        this.password = password;
        this.util = util;
        this.hostHandlerMatrix = hostHandlerMatrix;
        this.chunksize = chunksize != null ? chunksize : C8Defaults.CHUNK_DEFAULT_CONTENT_SIZE;
    }

    @SuppressWarnings("unchecked")
    protected synchronized C connect(final HostHandle hostHandle, final AccessType accessType, Service service) {
        HostHandler hostHandler = hostHandlerMatrix.get(service);
        Host host = hostHandler.get(hostHandle, accessType);
        while (true) {
            if (host == null) {
                hostHandler.reset();
                throw new C8DBException("Was not able to connect to any host");
            }
            final C connection = (C) host.connection();
            if (connection.isOpen()) {
                return connection;
            } else {
                try {
                    connection.open();
                    hostHandler.success();
                    if (user != null) {
                        authenticate(connection);
                    }
                    hostHandler.confirm();
                    return connection;
                } catch (final IOException e) {
                    hostHandler.fail();
                    if (hostHandle != null && hostHandle.getHost() != null) {
                        hostHandle.setHost(null);
                    }
                    final Host failedHost = host;
                    host = hostHandler.get(hostHandle, accessType);
                    if (host != null) {
                        LOGGER.warn(
                                String.format("Could not connect to %s or SSL Handshake failed. Try connecting to %s",
                                        failedHost.getDescription(), host.getDescription()));
                    } else {
                        LOGGER.error(e.getMessage(), e);
                        throw new C8DBException(e);
                    }
                }
            }
        }
    }

    protected abstract void authenticate(final C connection);

    @Override
    public void close() throws IOException {
        for (HostHandler hostHandler : hostHandlerMatrix.values()) {
            hostHandler.close();
        }
    }

    public R execute(final Request request, final HostHandle hostHandle, Service service) throws C8DBException {
        try {
            final C connection = connect(hostHandle, RequestUtils.determineAccessType(request), service);
            return execute(request, connection);
        } catch (final C8DBException e) {
            if (e instanceof C8DBRedirectException) {
                final String location = C8DBRedirectException.class.cast(e).getLocation();
                final HostDescription redirectHost = HostUtils.createFromLocation(location);
                HostHandler hostHandler = hostHandlerMatrix.get(service);
                hostHandler.closeCurrentOnError();
                hostHandler.fail();
                return execute(request, new HostHandle().setHost(redirectHost), service);
            } else {
                throw e;
            }
        }
    }

    protected abstract R execute(final Request request, C connection) throws C8DBException;

    protected void checkError(final Response response) throws C8DBException {
        ResponseUtils.checkError(util, response);
    }

    protected Response createResponse(final Message message) throws VPackParserException {
        final Response response = util.deserialize(message.getHead(), Response.class);
        if (message.getBody() != null) {
            response.setBody(message.getBody());
        }
        return response;
    }

    protected Message createMessage(final Request request) throws VPackParserException {
        final long id = mId.incrementAndGet();
        return new Message(id, util.serialize(request), request.getBody());
    }

    protected Collection<Chunk> buildChunks(final Message message) {
        final Collection<Chunk> chunks = new ArrayList<Chunk>();
        final VPackSlice head = message.getHead();
        int size = head.getByteSize();
        final VPackSlice body = message.getBody();
        if (body != null) {
            size += body.getByteSize();
        }
        final int n = size / chunksize;
        final int numberOfChunks = (size % chunksize != 0) ? (n + 1) : n;
        int off = 0;
        for (int i = 0; size > 0; i++) {
            final int len = Math.min(chunksize, size);
            final long messageLength = (i == 0 && numberOfChunks > 1) ? size : -1L;
            final Chunk chunk = new Chunk(message.getId(), i, numberOfChunks, messageLength, off, len);
            size -= len;
            off += len;
            chunks.add(chunk);
        }
        return chunks;
    }

}
