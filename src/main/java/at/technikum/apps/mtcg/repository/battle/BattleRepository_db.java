package at.technikum.apps.mtcg.repository.battle;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;

import java.sql.*;
import java.util.Optional;

public class BattleRepository_db implements BattleRepository {
    // DB CONNECTION
    private final Database database = new Database();

    // SQL STATEMENTS
    private final String START_BATTLE_SQL = "INSERT INTO battles (battle_id, player_a_fk, player_b_fk, start_time, status) VALUES (?,?,?,CURRENT_TIMESTAMP,'active')";
    private final String FIND_BATTLE_BY_ID_SQL = "SELECT battles.*, userA.user_id AS userA_id, userA.username AS userA_username, userA.password AS userA_password, userA.coins AS userA_coins, userA.eloRating AS userA_eloRating, userA.isAdmin AS userA_isAdmin, userB.user_id AS userB_id, userB.username AS userB_username, userB.password AS userB_password, userB.coins AS userB_coins, userB.eloRating AS userB_eloRating, userB.isAdmin AS userB_isAdmin, log_entry FROM battles LEFT JOIN users AS userA ON battles.player_a_fk = userA.user_id LEFT JOIN users AS userB ON battles.player_b_fk = userB.user_id LEFT JOIN battle_logs ON battles.battle_id = battle_logs.battle_fk WHERE battles.battle_id = ?";
    private final String START_LOG_SQL = "INSERT INTO battle_logs (battle_fk, log_entry) VALUES (?,?)";
    private final String ADD_TO_LOG_SQL = "UPDATE battle_logs SET log_entry = CONCAT(log_entry, ?) WHERE battle_fk = ?";

    private final String END_BATTLE_SQL = "UPDATE battles SET winner_fk = ?, status = 'completed' WHERE battle_id = ?";

    // IMPLEMENTATIONS
    @Override
    public Optional<BattleResult> findBattleById(String battleId) throws SQLException {
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
                            resultSet.getString("log_entry")
                    );
                    return Optional.of(battle);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding battle by ID: " + e.getMessage());
            throw new SQLException(e);
        }
        return Optional.empty();
    }

    @Override
    public boolean startBattle(String battleId, String hostId, String opponentId) throws SQLException {

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
                throw new SQLException(e);
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new SQLException(e);
        }
        return false;
    }

    @Override
    public boolean startLog(String battleId, String text) throws SQLException {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Insert into users
            try (PreparedStatement logCreateStatement = connection.prepareStatement(START_LOG_SQL, Statement.RETURN_GENERATED_KEYS)) {
                logCreateStatement.setString(1, battleId);
                logCreateStatement.setString(2, text);
                int logAffectedRows = logCreateStatement.executeUpdate();

                // Retrieve the generated key (user id)
                if (logAffectedRows == 1) {
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during Log creation: " + e.getMessage());
                throw new SQLException(e);
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new SQLException(e);
        }
        return false;
    }

    @Override
    public boolean addToLog(String battleId, String text) throws SQLException {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Insert into users
            try (PreparedStatement logCreateStatement = connection.prepareStatement(ADD_TO_LOG_SQL, Statement.RETURN_GENERATED_KEYS)) {
                logCreateStatement.setString(1, text);
                logCreateStatement.setString(2, battleId);
                int logAffectedRows = logCreateStatement.executeUpdate();

                // Retrieve the generated key (user id)
                if (logAffectedRows == 1) {
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during Log addition: " + e.getMessage());
                throw new SQLException(e);
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new SQLException(e);
        }
        return false;
    }

    @Override
    public boolean crownWinner(String battleId, String userId) throws SQLException {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            // Insert into users
            try (PreparedStatement logCreateStatement = connection.prepareStatement(END_BATTLE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                logCreateStatement.setString(1, userId);
                logCreateStatement.setString(2, battleId);
                int logAffectedRows = logCreateStatement.executeUpdate();

                // Retrieve the generated key (user id)
                if (logAffectedRows == 1) {
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during Log addition: " + e.getMessage());
                throw new SQLException(e);
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new SQLException(e);
        }
        return false;
    }


}
