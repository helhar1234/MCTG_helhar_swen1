package at.technikum.apps.mtcg.service;


import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.repository.stats.StatsRepository;

import java.util.Map;

public class StatsService {
    private final StatsRepository statsRepository;
    private final SessionService sessionService;

    public StatsService(StatsRepository statsRepository, SessionService sessionService) {
        this.statsRepository = statsRepository;
        this.sessionService = sessionService;
    }

    /**
     * Retrieves the total number of wins for a user.
     *
     * @param id The unique identifier of the user.
     * @return The total number of wins the user has achieved.
     */
    public int getUserWins(String id) {
        // Delegate to the statsRepository to get the user's wins
        return statsRepository.getUserWins(id);
    }


    /**
     * Retrieves the total number of battles played by a user.
     *
     * @param id The unique identifier of the user.
     * @return The total number of battles the user has participated in.
     */
    public int getUserBattles(String id) {
        // Delegate to the statsRepository to get the user's total number of battles
        return statsRepository.getUserBattles(id);
    }


    /**
     * Compiles various statistics for a given user.
     *
     * @param user The User object for whom statistics are being compiled.
     * @return A Map containing key statistics such as ELO rating, total wins, and total battles.
     */
    public Map<String, Object> getUserStats(User user) {
        // Retrieve wins and battles count for the user
        int wins = getUserWins(user.getId());
        int battles = getUserBattles(user.getId());

        // Compile the user statistics into a Map
        Map<String, Object> userStats = Map.of(
                "eloRating", user.getEloRating(), // User's ELO rating
                "wins", wins,                     // Total number of wins
                "totalBattles", battles           // Total number of battles played
        );

        return userStats;
    }

}
