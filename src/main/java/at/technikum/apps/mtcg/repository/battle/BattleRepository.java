package at.technikum.apps.mtcg.repository.battle;

import at.technikum.apps.mtcg.entity.BattleResult;

import java.sql.SQLException;
import java.util.Optional;

public interface BattleRepository {

    Optional<BattleResult> findBattleById(String battleId) throws SQLException;

    boolean startBattle(String battleId, String hostId, String opponentId) throws SQLException;

    boolean startLog(String battleId, String text) throws SQLException;

    boolean addToLog(String battleId, String text) throws SQLException;

    boolean crownWinner(String battleId, String userId) throws SQLException;
}
