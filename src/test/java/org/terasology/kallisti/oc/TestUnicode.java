package org.terasology.kallisti.oc;

import org.junit.Assert;
import org.junit.Test;

public class TestUnicode {
    private final ShimUnicode unicode = new ShimUnicode(null);

    @Test
    public void testChar() {
        Assert.assertEquals(
                "Abacus",
                unicode.__char('A', 'b', 'a', 'c', 'u', 's')
        );
    }

    @Test
    public void testReverse() {
        Assert.assertEquals(
                "revel",
                unicode.reverse("lever")
        );
    }

    @Test
    public void testSub() {
        Assert.assertEquals("hello", unicode.sub("hello", 1));
        Assert.assertEquals("hello", unicode.sub("hello", 1, -1));
        Assert.assertEquals("el", unicode.sub("hello", 2, 3));
        Assert.assertEquals("el", unicode.sub("hello", 2, -3));
        Assert.assertEquals("ice", unicode.sub("nice", 2));
    }

    @Test
    public void testWtrunc() {
        Assert.assertEquals("", unicode.wtrunc("hi", 0));
        Assert.assertEquals("", unicode.wtrunc("hi", 1));
        Assert.assertEquals("h", unicode.wtrunc("hi", 2));
    }

    @Test(expected = Exception.class)
    public void testWtruncOverflow() {
        unicode.wtrunc("hi", 3);
    }
}
