package at.technikum.apps.mtcg.service;


import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.stats.StatsRepository;
import at.technikum.server.http.Request;

import java.util.Map;

public class StatsService {
    private final StatsRepository statsRepository;
    private final SessionService sessionService;

    public StatsService(StatsRepository statsRepository, SessionService sessionService) {
        this.statsRepository = statsRepository;
        this.sessionService = sessionService;
    }

    public int getUserWins(String id) {
        return statsRepository.getUserWins(id);
    }

    public int getUserBattles(String id) {
        return statsRepository.getUserBattles(id);
    }

    public Map<String, Object> getUserStats(Request request) {
        User requester = sessionService.authenticateRequest(request);
        // Get user statistics
        int wins = getUserWins(requester.getId());
        int battles = getUserBattles(requester.getId());

        Map<String, Object> userStats = Map.of(
                "eloRating", requester.getEloRating(),
                "wins", wins,
                "totalBattles", battles
        );

        return userStats;
    }
}
