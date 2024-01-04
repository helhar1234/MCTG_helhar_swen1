package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.apps.mtcg.service.BattleLogic;

import java.util.Optional;

public interface BattleRepository {

    Optional<BattleResult> findBattleById(String battleId);

    boolean startBattle(String battleId, String hostId, String opponentId);
}
