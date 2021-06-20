package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DictionaryLoaderTest {
    private static DictionaryLoader loader;

    @BeforeAll
    static void setUpForAll() {
        loader = new DictionaryLoader();
    }

    @Test
    void testWrongPath() {
        assertEquals(0, loader.load("a").size());
    }

    @Test
    void testNullPath() {
        assertThrows(NullPointerException.class, () -> loader.load(null));
    }
}
