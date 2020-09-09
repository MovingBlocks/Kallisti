// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

public final class KallistiMath {
    private KallistiMath() {

    }

    /**
     * log2, rounding up
     */
    public static int log2up(int v) {
        if (v < 2) {
            return 0;
        }

        int x = 0;
        v -= 1;
        while (v > 0) {
            v >>= 1;
            x++;
        }
        return x;
    }

    /**
     * log2, rounding down
     */
    public static int log2(int v) {
        if (v < 2) {
            return 0;
        }

        int x = 0;
        while (v > 0) {
            v >>= 1;
            x++;
        }
        return x - 1;
    }

    /**
     * Get the smallest power of two containing the given value.
     *
     * @param v The given value.
     */
    public static int smallestContainingPowerTwo(int v) {
        v = v - 1;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return v + 1;
    }
}
