package at.technikum.apps.mtcg.repository.battle;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.BattleResult;
import at.technikum.apps.mtcg.entity.User;
import at.technikum.server.http.HttpStatus;

import java.sql.*;
import java.util.Optional;

public class BattleRepository_db implements BattleRepository {
    // DB CONNECTION
    private final Database database;

    public BattleRepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String START_BATTLE_SQL = "INSERT INTO battles (battle_id, player_a_fk, player_b_fk, start_time, status) VALUES (?,?,?,CURRENT_TIMESTAMP,'active')";
    private final String FIND_BATTLE_BY_ID_SQL = "SELECT battles.*, userA.user_id AS userA_id, userA.username AS userA_username, userA.password AS userA_password, userA.coins AS userA_coins, userA.eloRating AS userA_eloRating, userA.isAdmin AS userA_isAdmin, userB.user_id AS userB_id, userB.username AS userB_username, userB.password AS userB_password, userB.coins AS userB_coins, userB.eloRating AS userB_eloRating, userB.isAdmin AS userB_isAdmin, log_entry FROM battles LEFT JOIN users AS userA ON battles.player_a_fk = userA.user_id LEFT JOIN users AS userB ON battles.player_b_fk = userB.user_id LEFT JOIN battle_logs ON battles.battle_id = battle_logs.battle_fk WHERE battles.battle_id = ?";
    private final String START_LOG_SQL = "INSERT INTO battle_logs (battle_fk, log_entry) VALUES (?,?)";
    private final String ADD_TO_LOG_SQL = "UPDATE battle_logs SET log_entry = CONCAT(log_entry, ?) WHERE battle_fk = ?";

    private final String END_BATTLE_SQL = "UPDATE battles SET winner_fk = ?, status = 'completed' WHERE battle_id = ?";

    // IMPLEMENTATIONS

    /**
     * Retrieves a battle result by its ID from the database.
     *
     * @param battleId The unique identifier of the battle to be retrieved.
     * @return An Optional containing the BattleResult if found, or an empty Optional if not found.
     * @throws HttpStatusException If there is an SQL error or database connection issue.
     */
    @Override
    public Optional<BattleResult> findBattleById(String battleId) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BATTLE_BY_ID_SQL)) {
            // Set the battle ID in the SQL query
            statement.setString(1, battleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Construct User objects for both players using data from the database
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

                    // Determine the winner of the battle, if any
                    User winner = null;
                    String winnerId = resultSet.getString("winner_fk");
                    if (winnerId != null) {
                        winner = winnerId.equals(playerA.getId()) ? playerA : (winnerId.equals(playerB.getId()) ? playerB : null);
                    }

                    // Construct and return the BattleResult object
                    BattleResult battle = new BattleResult(
                            resultSet.getString("battle_id"),
                            playerA,
                            playerB,
                            resultSet.getString("status"),
                            winner,
                            resultSet.getString("start_time"),
                            resultSet.getString("log_entry")
                    );
                    return Optional.of(battle);
                }
            } catch (SQLException e) {
                // Handle SQL exceptions during the query execution
                System.out.println("Error finding battle by ID: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding battle by ID: " + e);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty();
    }


    /**
     * Starts a new battle by inserting battle details into the database.
     *
     * @param battleId   The unique identifier of the battle to be started.
     * @param hostId     The ID of the host player.
     * @param opponentId The ID of the opponent player.
     * @return True if the battle is successfully started, false otherwise.
     * @throws HttpStatusException If there is an error during the battle creation or a database connection issue.
     */
    @Override
    public boolean startBattle(String battleId, String hostId, String opponentId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to create a new battle
            try (PreparedStatement battleCreateStatement = connection.prepareStatement(START_BATTLE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                battleCreateStatement.setString(1, battleId);
                battleCreateStatement.setString(2, hostId);
                battleCreateStatement.setString(3, opponentId);
                int battleAffectedRows = battleCreateStatement.executeUpdate();

                // Check if the battle record is successfully created
                if (battleAffectedRows == 1) {
                    // Commit the transaction if the battle is successfully created
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                // Rollback the transaction in case of any SQL error during battle creation
                connection.rollback();
                System.out.println("Error during battle creation: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during battle creation: " + e);
            } finally {
                // Reset auto-commit to its default state
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false; // Return false if the battle is not successfully started
    }


    /**
     * Starts a log for a specific battle with initial text.
     *
     * @param battleId The unique identifier of the battle.
     * @param text     The initial text to be logged.
     * @return True if the log is successfully started, false otherwise.
     * @throws HttpStatusException If there is an error during log creation or a database connection issue.
     */
    @Override
    public boolean startLog(String battleId, String text) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to start a new log
            try (PreparedStatement logCreateStatement = connection.prepareStatement(START_LOG_SQL, Statement.RETURN_GENERATED_KEYS)) {
                logCreateStatement.setString(1, battleId);
                logCreateStatement.setString(2, text);
                int logAffectedRows = logCreateStatement.executeUpdate();

                // Commit the transaction if the log is successfully created
                if (logAffectedRows == 1) {
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                // Rollback the transaction in case of any SQL error
                connection.rollback();
                System.out.println("Error during Log creation: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during Log creation: " + e);
            } finally {
                // Reset auto-commit to its default state
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false;
    }


    /**
     * Adds a new entry to the battle log in the database.
     *
     * @param battleId The unique identifier of the battle.
     * @param text     The log text to be added.
     * @return True if the log entry is successfully added, false otherwise.
     * @throws HttpStatusException If there is an error during log addition or a database connection issue.
     */
    @Override
    public boolean addToLog(String battleId, String text) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction for database integrity
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to add a new entry to the battle log
            try (PreparedStatement logCreateStatement = connection.prepareStatement(ADD_TO_LOG_SQL, Statement.RETURN_GENERATED_KEYS)) {
                logCreateStatement.setString(1, text); // Set the log text
                logCreateStatement.setString(2, battleId); // Set the battle ID
                int logAffectedRows = logCreateStatement.executeUpdate();

                // Check if the log entry was successfully added
                if (logAffectedRows == 1) {
                    connection.commit(); // Commit the transaction to save changes
                    return true;
                }
            } catch (SQLException e) {
                // Rollback the transaction in case of any SQL error
                connection.rollback();
                System.out.println("Error during Log addition: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during Log addition: " + e);
            } finally {
                // Reset auto-commit to its default state
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false; // Return false if the log entry is not successfully added
    }


    /**
     * Marks a user as the winner of a specific battle.
     *
     * @param battleId The unique identifier of the battle.
     * @param userId   The ID of the user who won the battle.
     * @return True if the winner is successfully marked, false otherwise.
     * @throws HttpStatusException If there is an error during the process or a database connection issue.
     */
    @Override
    public boolean crownWinner(String battleId, String userId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to mark the winner of the battle
            try (PreparedStatement logCreateStatement = connection.prepareStatement(END_BATTLE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                logCreateStatement.setString(1, userId);
                logCreateStatement.setString(2, battleId);
                int logAffectedRows = logCreateStatement.executeUpdate();

                // Commit the transaction if the winner is successfully marked
                if (logAffectedRows == 1) {
                    connection.commit();
                    return true;
                }
            } catch (SQLException e) {
                // Rollback the transaction in case of any SQL error
                connection.rollback();
                System.out.println("Error during Log addition: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during Log addition: " + e);
            } finally {
                // Reset auto-commit to its default state
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false;
    }


}
