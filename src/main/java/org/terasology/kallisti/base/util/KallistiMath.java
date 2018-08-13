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
 * Utility methods pertaining to math calculations.
 */
public final class KallistiMath {
    private KallistiMath() {

    }

    /**
     * @return log2(v), rounding up
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
     * @return log2(v), rounding down
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
