package at.technikum.apps.mtcg.repository.battle;

import at.technikum.apps.mtcg.entity.BattleResult;

import java.util.Optional;

public interface BattleRepository {

    Optional<BattleResult> findBattleById(String battleId);

    boolean startBattle(String battleId, String hostId, String opponentId);

    boolean startLog(String battleId, String text);

    boolean addToLog(String battleId, String text);

    boolean crownWinner(String battleId, String userId);
}
