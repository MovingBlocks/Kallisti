// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

public final class KallistiColor {
    private KallistiColor() {

    }

    /**
     * Get the color distance of two colours in 0x??RRGGBB format.
     *
     * @param a First color.
     * @param b Second color.
     * @return The distance.
     */
    public static double distance(int a, int b) {
        float r1 = ((a >> 16) & 0xFF) / 255.0f;
        float r2 = ((b >> 16) & 0xFF) / 255.0f;
        float g1 = ((a >> 8) & 0xFF) / 255.0f;
        float g2 = ((b >> 8) & 0xFF) / 255.0f;
        float b1 = (a & 0xFF) / 255.0f;
        float b2 = (b & 0xFF) / 255.0f;

        float rd = r1 - r2;
        float gd = g1 - g2;
        float bd = b1 - b2;
        float rAvg = (r1 + r2) / 2;
        return Math.sqrt((2 + rAvg) * rd * rd + 4 * gd * gd + (2 + (1 - rAvg)) * bd * bd);
    }
}
