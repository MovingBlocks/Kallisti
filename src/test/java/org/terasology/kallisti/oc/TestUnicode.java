package org.terasology.kallisti.oc;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestUnicode {
    private final ShimUnicode unicode = new ShimUnicode(null);

    @Test
    public void testChar() {
        assertEquals(
                "Abacus",
                unicode.__char('A', 'b', 'a', 'c', 'u', 's')
        );
    }

    @Test
    public void testReverse() {
        assertEquals(
                "revel",
                unicode.reverse("lever")
        );
    }

    @Test
    public void testSub() {
        assertEquals("hello", unicode.sub("hello", 1, Optional.empty()));
        assertEquals("hello", unicode.sub("hello", 1, Optional.of(-1)));
        assertEquals("el", unicode.sub("hello", 2, Optional.of(3)));
        assertEquals("el", unicode.sub("hello", 2, Optional.of(-3)));
        assertEquals("ice", unicode.sub("nice", 2, Optional.empty()));
    }

    @Test
    public void testWtrunc() {
        assertEquals("", unicode.wtrunc("hi", 0));
        assertEquals("", unicode.wtrunc("hi", 1));
        assertEquals("h", unicode.wtrunc("hi", 2));
    }

    @Test
    public void testWtruncOverflow() {
        assertThrows(Exception.class, () -> {
            unicode.wtrunc("hi", 3);
        });
    }
}
