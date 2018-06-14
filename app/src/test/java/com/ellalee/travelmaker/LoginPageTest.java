package com.ellalee.travelmaker;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoginPageTest {
    @Test
    public void LoginPageTest() {
        String result = LoginPage.getEmail();
        assertEquals("EmailStuff", result);
    }
}


