package at.technikum.apps.mtcg.service;

import at.technikum.apps.mtcg.entity.UserStats;
import at.technikum.apps.mtcg.repository.scoreboard.ScoreboardRepository;

import java.sql.SQLException;

public class ScoreboardService {

    private final ScoreboardRepository scoreboardRepository;

    public ScoreboardService(ScoreboardRepository scoreboardRepository) {
        this.scoreboardRepository = scoreboardRepository;
    }

    public UserStats[] getScoreboard() throws SQLException {
        return scoreboardRepository.getScoreboard();
    }
}
