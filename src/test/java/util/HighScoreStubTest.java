package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.HighScore;

public class HighScoreStubTest {

    @Test
    public void testHighScoreStubDate() {
        // Stub: HighScore with fixed date
        HighScore stubScore = new HighScore("Stub", 123) {
            @Override
            public java.time.LocalDate getDate() {
                return java.time.LocalDate.of(2025, 1, 1);
            }
        };
        assertEquals("2025-01-01", stubScore.getDate().toString());
    }
}