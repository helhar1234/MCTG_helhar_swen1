package at.technikum.apps.mtcg.repository.stats;

public interface StatsRepository {
    int getUserWins(String id);

    int getUserBattles(String id);
}
