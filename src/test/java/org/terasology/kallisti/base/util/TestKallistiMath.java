package org.terasology.kallisti.base.util;

import org.junit.Assert;
import org.junit.Test;

public class TestKallistiMath {
    @Test
    public void log2() {
        Assert.assertEquals(0, KallistiMath.log2(1));
        Assert.assertEquals(1, KallistiMath.log2(2));
        Assert.assertEquals(1, KallistiMath.log2(3));
        Assert.assertEquals(2, KallistiMath.log2(4));
        Assert.assertEquals(2, KallistiMath.log2(5));
        Assert.assertEquals(2, KallistiMath.log2(7));
        Assert.assertEquals(3, KallistiMath.log2(8));
        Assert.assertEquals(3, KallistiMath.log2(9));
    }

    @Test
    public void log2up() {
        Assert.assertEquals(0, KallistiMath.log2up(1));
        Assert.assertEquals(1, KallistiMath.log2up(2));
        Assert.assertEquals(2, KallistiMath.log2up(3));
        Assert.assertEquals(2, KallistiMath.log2up(4));
        Assert.assertEquals(3, KallistiMath.log2up(5));
        Assert.assertEquals(3, KallistiMath.log2up(7));
        Assert.assertEquals(3, KallistiMath.log2up(8));
        Assert.assertEquals(4, KallistiMath.log2up(9));
    }

    @Test
    public void nextPowerOfTwo() {
        Assert.assertEquals(8, KallistiMath.smallestContainingPowerTwo(5));
        Assert.assertEquals(8, KallistiMath.smallestContainingPowerTwo(6));
        Assert.assertEquals(8, KallistiMath.smallestContainingPowerTwo(7));
        Assert.assertEquals(8, KallistiMath.smallestContainingPowerTwo(8));

        Assert.assertEquals(1073741824, KallistiMath.smallestContainingPowerTwo(536870913));
        Assert.assertEquals(1073741824, KallistiMath.smallestContainingPowerTwo(1073741823));
        Assert.assertEquals(1073741824, KallistiMath.smallestContainingPowerTwo(1073741824));
    }
}
