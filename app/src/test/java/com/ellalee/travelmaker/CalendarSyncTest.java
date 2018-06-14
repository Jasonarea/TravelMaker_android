package com.ellalee.travelmaker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CalendarSyncTest {
    @Test
    public void CalendarSyncTest() {
        String newCalId = CalendarSync.newCalId;
        assertEquals(newCalId, CalendarSync.newCalId);
    }
}
