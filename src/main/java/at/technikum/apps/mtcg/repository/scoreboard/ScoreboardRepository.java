package at.technikum.apps.mtcg.repository.scoreboard;

import at.technikum.apps.mtcg.dto.UserStats;

public interface ScoreboardRepository {

    UserStats[] getScoreboard();
}
