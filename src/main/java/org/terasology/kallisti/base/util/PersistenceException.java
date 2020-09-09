// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

public class PersistenceException extends Exception {
    public PersistenceException(String s) {
        super(s);
    }

    public PersistenceException(Throwable e) {
        super(e);
    }
}
