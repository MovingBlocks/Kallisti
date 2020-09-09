// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface for components which can be synchronized across the network, from the server to the client.
 */
public interface Synchronizable {
    /**
     * Check if the component has a ready synchronization packet of a given type.
     *
     * @param type The type.
     * @return Whether a synchronization packet can be made or not.
     */
    boolean hasSyncPacket(Type type);

    /**
     * Write the synchronization packet to the given output stream.
     *
     * @param type The packet type.
     * @param stream The given output stream.
     * @throws IOException Upon packet writing issues.
     */
    void writeSyncPacket(Type type, OutputStream stream) throws IOException;

    enum Type {
        /**
         * Intial update for a given client.
         */
        INITIAL,
        /**
         * Any future update for a given client.
         */
        DELTA
    }

    /**
     * An interface for classes capable of receiving data from a matching Synchronizable.
     * <p>
     * Generally, Receivers should store the binding to sources, as well as maintain synchronization of source data to
     * the Receiver.
     */
    interface Receiver {
        void update(InputStream stream) throws IOException;
    }
}
