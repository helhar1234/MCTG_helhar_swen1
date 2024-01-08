package at.technikum.apps.mtcg.repository.scoreboard;

import at.technikum.apps.mtcg.entity.UserStats;

import java.sql.SQLException;

public interface ScoreboardRepository {

    UserStats[] getScoreboard() throws SQLException;
}
