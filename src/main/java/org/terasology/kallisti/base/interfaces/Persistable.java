// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.util.PersistenceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface for components whose data can be stored, persisted and read, including across version upgrades.
 */
public interface Persistable {
    void persist(OutputStream data) throws IOException, PersistenceException;

    void unpersist(InputStream data) throws IOException, PersistenceException;
}
