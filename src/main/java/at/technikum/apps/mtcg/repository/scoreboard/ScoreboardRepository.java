package at.technikum.apps.mtcg.repository.scoreboard;

import at.technikum.apps.mtcg.entity.UserStats;

public interface ScoreboardRepository {

    UserStats[] getScoreboard();
}
