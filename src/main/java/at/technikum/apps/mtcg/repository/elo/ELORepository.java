package at.technikum.apps.mtcg.repository.elo;

public interface ELORepository {
    boolean updateELO(String userId, int eloToAdd);
}
