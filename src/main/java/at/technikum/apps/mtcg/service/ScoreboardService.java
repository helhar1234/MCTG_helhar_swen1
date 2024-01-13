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

    public UserStats[] getScoreboard(User user) {
        // Maybe use user for specific scoreboard Query?
        return scoreboardRepository.getScoreboard();
    }
}
