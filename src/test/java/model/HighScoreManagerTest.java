package model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import util.HighScoreManager;

public class HighScoreManagerTest {

    @BeforeEach
    public void resetScores() {
        HighScoreManager.getInstance().clearScores();
    }

    @Test
    public void testAddAndGetTopScores() {
        HighScoreManager mgr = HighScoreManager.getInstance();
        mgr.addHighScore(new HighScore("Alice", 100));
        mgr.addHighScore(new HighScore("Bob", 200));
        assertEquals(2, mgr.getTopScores().size());
        assertEquals("Bob", mgr.getTopScores().get(0).getPlayerName());
    }

    @Test
    public void testClearScores() {
        HighScoreManager mgr = HighScoreManager.getInstance();
        mgr.addHighScore(new HighScore("Test", 50));
        mgr.clearScores();
        assertEquals(0, mgr.getTopScores().size());
    }
}