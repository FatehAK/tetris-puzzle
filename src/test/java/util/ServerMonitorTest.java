package util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ServerMonitorTest {

    private ServerMonitor monitor;

    @BeforeEach
    public void setUp() {
        monitor = new ServerMonitor();
    }

    @Test
    public void testGetServerAddress() {
        String address = monitor.getServerAddress();
        assertEquals("localhost:3000", address);
    }

    @Test
    public void testIsServerRunningFalse() {
        // This test will pass only if nothing is running on localhost:3000
        // If a server is running, change the port or skip this test
        assertFalse(monitor.isServerRunning());
    }
}