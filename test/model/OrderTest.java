package model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OrderTest {
    private static Order order;

    
    @BeforeAll
    static void setUpForClass() {
        order = new Order("testId", "testName", "testDate", 2, 3.0, new String[] { "testItem1", "testItem2" });
    }

    @Test
    void testGetId() {
        assertEquals("testId", order.getId());
    }

    @Test
    void testGetName() {
        assertEquals("testName", order.getName());
    }

    @Test
    void testGetDate() {
        assertEquals("testDate", order.getDate());
    }

    @Test
    void testGetNumOfItems() {
        assertEquals(2, order.getNumOfItems());
    }

    @Test
    void testGetTotal() {
        assertEquals(3.0, order.getTotal());

    }

    @Test
    void testGetItems() {
        String[] expected = new String[] { "testItem1", "testItem2" };
        assertArrayEquals(expected, order.getItems());
    }
}
