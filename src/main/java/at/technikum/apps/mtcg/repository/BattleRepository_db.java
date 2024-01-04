package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BattleRepository_db implements BattleRepository{
    private final Database database = new Database();

    private final String START_BATTLE_SQL = "INSERT INTO battles (battle_id, player_a_fk, player_b_fk, start_time, status) VALUES (?,?,?,CURRENT_TIMESTAMP,'active')";

    private final String FIND_BATTLE_BY_ID_SQL = "SELECT battles.*, userA.user_id AS userA_id, userA.username AS userA_username, userA.password AS userA_password, userA.coins AS userA_coins, userA.eloRating AS userA_eloRating, userA.isAdmin AS userA_isAdmin, userB.user_id AS userB_id, userB.username AS userB_username, userB.password AS userB_password, userB.coins AS userB_coins, userB.eloRating AS userB_eloRating, userB.isAdmin AS userB_isAdmin\n FROM battles LEFT JOIN users AS userA ON battles.player_a_fk = userA.user_id LEFT JOIN users AS userB ON battles.player_b_fk = userB.user_id WHERE battles.battle_id = ?";


    @Override
    public Optional<BattleResult> findBattleById(String battleId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BATTLE_BY_ID_SQL)) {
            statement.setString(1, battleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User playerA = new User(
                            resultSet.getString("userA_id"),
                            resultSet.getString("userA_username"),
                            resultSet.getString("userA_password"),
                            resultSet.getInt("userA_coins"),
                            resultSet.getInt("userA_eloRating"),
                            resultSet.getBoolean("userA_isAdmin")
                    );

                    User playerB = new User(
                            resultSet.getString("userB_id"),
                            resultSet.getString("userB_username"),
                            resultSet.getString("userB_password"),
                            resultSet.getInt("userB_coins"),
                            resultSet.getInt("userB_eloRating"),
                            resultSet.getBoolean("userB_isAdmin")
                    );

                    User winner = null;
                    String winnerId = resultSet.getString("winner_fk");
                    if (winnerId != null) {
                        if (winnerId.equals(playerA.getId())) {
                            winner = playerA;
                        } else if (winnerId.equals(playerB.getId())) {
                            winner = playerB;
                        }
                    }

                    BattleResult battle = new BattleResult(
                            resultSet.getString("battle_id"),
                            playerA,
                            playerB,
                            resultSet.getString("status"),
                            winner,
                            resultSet.getTimestamp("start_time"),
                            "battleLog"
                    );
                    return Optional.of(battle);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding battle by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean startBattle(String battleId, String hostId, String opponentId) {

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Insert into users
            try (PreparedStatement battleCreateStatement = connection.prepareStatement(START_BATTLE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                battleCreateStatement.setString(1, battleId);
                battleCreateStatement.setString(2, hostId);
                battleCreateStatement.setString(3, opponentId);
                int battleAffectedRows = battleCreateStatement.executeUpdate();

                // Retrieve the generated key (user id)
                if (battleAffectedRows == 1) {
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during battle creation: " + e.getMessage());
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return false;
    }


}
