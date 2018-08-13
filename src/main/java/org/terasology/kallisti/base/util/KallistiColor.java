/*
 * Copyright 2018 Adrian Siekierka, MovingBlocks
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

package org.terasology.kallisti.base.util;

/**
 * Utility methods pertaining to color handling.
 */
public final class KallistiColor {
    private KallistiColor() {

    }

    /**
     * Get the color distance of two colours in 0x??RRGGBB format.
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
        return Math.sqrt((2 + rAvg) * rd*rd + 4 * gd*gd + (2 + (1 - rAvg)) * bd*bd);
    }
}
