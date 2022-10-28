/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.internal.tpc;

import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A server socket that is asynchronous. So accepting incoming connections does not block,
 * but are executed on an {@link Eventloop}.
 */
public abstract class AsyncServerSocket implements Closeable {

    /**
     * Allows for objects to be bound to this AsyncServerSocket. Useful for the lookup of services and other dependencies.
     */
    public final ConcurrentMap context = new ConcurrentHashMap();

    protected final ILogger logger = Logger.getLogger(getClass());
    protected final AtomicBoolean closed = new AtomicBoolean(false);

    public final SocketAddress localAddress() {
        try {
            return localAddress0();
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    protected abstract SocketAddress localAddress0() throws Exception;

    /**
     * Gets the {@link Eventloop} this ServerSocket belongs to.
     * <p/>
     * The returned value will never be <code>null</code>
     *
     * @return the Eventloop.
     */
    public abstract Eventloop eventloop();


    public abstract int localPort();

    /**
     * Checks if the SO_REUSEPORT option has been set.
     * <p/>
     * When SO_REUSEPORT isn't supported, false is returned.
     *
     * @return true if SO_REUSEPORT is enabled.
     * @throws UncheckedIOException if something failed with configuring the socket
     */
    public abstract boolean isReusePort();

    /**
     * Sets the SO_REUSEPORT option.
     * <p/>
     * It could be that this call is ignored (e.g. Nio + Java 8).
     *
     * @param reusePort if the SO_REUSEPORT option should be enabled.
     * @throws UncheckedIOException if something failed with configuring the socket
     */
    public abstract void reusePort(boolean reusePort);

    /**
     * Checks if the SO_REUSEADDR option has been set.
     *
     * @return true if SO_REUSEADDR is enabled.
     * @throws UncheckedIOException if something failed with configuring the socket
     */
    public abstract boolean isReuseAddress();

    /**
     * Sets the SO_REUSEADDR option.
     *
     * @param reuseAddress if the SO_REUSEADDR option should be enabled.
     * @throws UncheckedIOException if something failed with configuring the socket
     */
    public abstract void reuseAddress(boolean reuseAddress);

    /**
     * Sets the receivebuffer size in bytes.
     *
     * @param size the receivebuffer size in bytes.
     * @throws IllegalArgumentException when the size isn't positive.
     * @throws UncheckedIOException if something failed with configuring the socket
     */
    public abstract void receiveBufferSize(int size);

    /**
     * Gets the receivebuffer size in bytes.
     *
     * @return the size of the receive buffer.
     * @throws UncheckedIOException if something failed with configuring the socket
     */
    public abstract int receiveBufferSize();

    /**
     * Binds this AsyncServerSocket to the local.
     *
     * @param local the local SocketAddress.
     * @throws UncheckedIOException if something failed with configuring the socket
     */
    public abstract void bind(SocketAddress local);

    public abstract void listen(int backlog);

    /**
     * Closes the AsyncServerSocket.
     * <p/>
     * If the AsyncServerSocket is already closed, it is ignored.
     * <p/>
     * This method is thread-safe.
     * <p/>
     * This method doesn't throw an exception.
     */
    @Override
    public final void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Closing  " + this);
        }

        try {
            close0();
        } catch (Exception e) {
            logger.warning(e);
        }
    }

    protected abstract void close0() throws IOException;

    /**
     * Checks if the AsyncServerSocket is closed.
     *
     * @return true if closed, false otherwise.
     */
    public final boolean isClosed() {
        return closed.get();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + localAddress() + "]";
    }
}