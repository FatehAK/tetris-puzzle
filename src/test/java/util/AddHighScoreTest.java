package util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import model.HighScore;
import model.HighScoreRepository;

@ExtendWith(MockitoExtension.class)
public class AddHighScoreTest {

    @Test
    public void testAddHighScoreCalled() {
        HighScoreRepository mockRepo = mock(HighScoreRepository.class);
        HighScore score = new HighScore("TestUser", 999);

        mockRepo.addHighScore(score);

        // Verify that addHighScore was called with the correct score
        verify(mockRepo).addHighScore(score);
    }
}