package org.terasology.kallisti.base.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestKallistiMath {
    @Test
    public void log2() {
        assertEquals(0, KallistiMath.log2(1));
        assertEquals(1, KallistiMath.log2(2));
        assertEquals(1, KallistiMath.log2(3));
        assertEquals(2, KallistiMath.log2(4));
        assertEquals(2, KallistiMath.log2(5));
        assertEquals(2, KallistiMath.log2(7));
        assertEquals(3, KallistiMath.log2(8));
        assertEquals(3, KallistiMath.log2(9));
    }

    @Test
    public void log2up() {
        assertEquals(0, KallistiMath.log2up(1));
        assertEquals(1, KallistiMath.log2up(2));
        assertEquals(2, KallistiMath.log2up(3));
        assertEquals(2, KallistiMath.log2up(4));
        assertEquals(3, KallistiMath.log2up(5));
        assertEquals(3, KallistiMath.log2up(7));
        assertEquals(3, KallistiMath.log2up(8));
        assertEquals(4, KallistiMath.log2up(9));
    }

    @Test
    public void nextPowerOfTwo() {
        assertEquals(8, KallistiMath.smallestContainingPowerTwo(5));
        assertEquals(8, KallistiMath.smallestContainingPowerTwo(6));
        assertEquals(8, KallistiMath.smallestContainingPowerTwo(7));
        assertEquals(8, KallistiMath.smallestContainingPowerTwo(8));

        assertEquals(1073741824, KallistiMath.smallestContainingPowerTwo(536870913));
        assertEquals(1073741824, KallistiMath.smallestContainingPowerTwo(1073741823));
        assertEquals(1073741824, KallistiMath.smallestContainingPowerTwo(1073741824));
    }
}
