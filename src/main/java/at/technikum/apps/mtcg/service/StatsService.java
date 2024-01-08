package at.technikum.apps.mtcg.service;


import at.technikum.apps.mtcg.repository.stats.StatsRepository;
import at.technikum.apps.mtcg.repository.stats.StatsRepository_db;

public class StatsService {
    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    public int getUserWins(String id) {
        return statsRepository.getUserWins(id);
    }

    public int getUserBattles(String id) {
        return statsRepository.getUserBattles(id);
    }
}
