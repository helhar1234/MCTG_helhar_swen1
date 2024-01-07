package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.UserStats;
import at.technikum.apps.mtcg.repository.scoreboard.ScoreboardRepository;
import at.technikum.apps.mtcg.repository.scoreboard.ScoreboardRepository_db;

public class ScoreboardService {

    private final ScoreboardRepository scoreboardRepository;

    public ScoreboardService() {
        this.scoreboardRepository = new ScoreboardRepository_db();
    }

    // FOR TESTING
    public ScoreboardService(ScoreboardRepository scoreboardRepository) {
        this.scoreboardRepository = scoreboardRepository;
    }

    public UserStats[] getScoreboard() {
        return scoreboardRepository.getScoreboard();
    }
}
