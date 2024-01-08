package at.technikum.apps.mtcg.repository.stats;

import java.sql.SQLException;

public interface StatsRepository {
    int getUserWins(String id) throws SQLException;

    int getUserBattles(String id) throws SQLException;
}
