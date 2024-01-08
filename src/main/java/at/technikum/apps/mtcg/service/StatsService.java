package at.technikum.apps.mtcg.service;


import at.technikum.apps.mtcg.repository.stats.StatsRepository;

import java.sql.SQLException;

public class StatsService {
    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public int getUserWins(String id) throws SQLException {
        return statsRepository.getUserWins(id);
    }

    public int getUserBattles(String id) throws SQLException {
        return statsRepository.getUserBattles(id);
    }
}
