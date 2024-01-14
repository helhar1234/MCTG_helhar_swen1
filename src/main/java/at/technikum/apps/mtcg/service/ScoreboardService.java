package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.dto.UserStats;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.scoreboard.ScoreboardRepository;

public class ScoreboardService {

    private final ScoreboardRepository scoreboardRepository;
    private final SessionService sessionService;

    public ScoreboardService(ScoreboardRepository scoreboardRepository, SessionService sessionService) {
        this.scoreboardRepository = scoreboardRepository;
        this.sessionService = sessionService;
    }

    /**
     * Retrieves the scoreboard containing user statistics.
     *
     * @param user The user requesting the scoreboard. Can be used for user-specific queries in the future.
     * @return An array of UserStats representing the scoreboard.
     */
    public UserStats[] getScoreboard(User user) {
        // Currently, the method returns the general scoreboard
        // In the future, it could be modified to return a user-specific scoreboard based on the provided user
        return scoreboardRepository.getScoreboard();
    }

}
