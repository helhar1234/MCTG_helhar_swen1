package at.technikum.apps.mtcg.repository.card;

import at.technikum.apps.mtcg.customExceptions.HttpStatusException;
import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.dto.PackageCard;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.server.http.HttpStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardRepository_db implements CardRepository {
    // DB CONNECTION
    private final Database database;

    public CardRepository_db(Database database) {
        this.database = database;
    }

    // SQL STATEMENTS
    private final String SAVE_CARD_SQL = "INSERT INTO cards (card_id, name, damage, elementtype, cardtype) VALUES (?,?,?,?,?)";
    private final String FIND_CARD_BY_ID_SQL = "SELECT * FROM cards WHERE card_id = ?";
    private final String FIND_CARDS_OF_USER_SQL = "SELECT c.* FROM cards c JOIN user_cards uc ON c.card_id = uc.card_fk WHERE user_fk = ?";
    private final String FIND_CARDS_OF_USER_DECK_SQL = "SELECT c.* FROM cards c JOIN user_cards uc ON c.card_id = uc.card_fk WHERE user_fk = ? AND uc.indeck is true";
    private final String CHECK_CARD_IN_STACK_SQL = "SELECT COUNT(*) FROM user_cards WHERE user_fk = ? AND card_fk = ?";
    private final String CHECK_CARD_IN_DECK_SQL = "SELECT COUNT(*) FROM user_cards WHERE user_fk = ? AND card_fk = ? AND indeck is true";
    private final String ADD_CARDS_TO_DECK_SQL = "UPDATE user_cards SET indeck = ? WHERE card_fk = ? AND user_fk = ?";
    private final String RESET_USER_DECK_SQL = "UPDATE user_cards SET indeck = ? WHERE user_fk = ?";
    private final String DELETE_CARD_FROM_STACK_SQL = "DELETE FROM user_cards WHERE user_fk = ? AND card_fk = ?";
    private final String ADD_CARD_TO_STACK_SQL = "INSERT INTO user_cards (user_fk, card_fk) VALUES (?, ?)";
    private final String GET_CARD_NON_POSSESSED_SQL = "SELECT * FROM cards WHERE card_id NOT IN (SELECT card_fk FROM user_cards)";

    // IMPLEMENTATIONS

    /**
     * Saves a card to the database.
     *
     * @param card The PackageCard object to be saved.
     * @return True if the card is successfully saved, false otherwise.
     * @throws HttpStatusException If there is an error during the card saving process or a database connection issue.
     */
    @Override
    public boolean saveCard(PackageCard card) {
        boolean success = false;

        // Determine the element type and card type based on the card's name
        String elementtype = card.getName().toLowerCase().contains("water") ? "water" :
                card.getName().toLowerCase().contains("fire") ? "fire" : "normal";
        String cardtype = card.getName().toLowerCase().contains("spell") ? "spell" : "monster";

        try (Connection connection = database.getConnection()) {
            // Start a transaction for database integrity
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to save the card
            try (PreparedStatement saveCardStatement = connection.prepareStatement(SAVE_CARD_SQL)) {
                saveCardStatement.setString(1, card.getId());
                saveCardStatement.setString(2, card.getName());
                saveCardStatement.setDouble(3, card.getDamage());
                saveCardStatement.setString(4, elementtype);
                saveCardStatement.setString(5, cardtype);

                // Execute the update and check the affected rows
                int affectedRows = saveCardStatement.executeUpdate();
                if (affectedRows == 1) {
                    // Commit the transaction if the card is successfully saved
                    connection.commit();
                    success = true;
                } else {
                    // Rollback the transaction if the save operation didn't affect any rows
                    connection.rollback();
                }
            } catch (SQLException e) {
                // Rollback the transaction in case of any SQL error
                connection.rollback();
                System.out.println("Error during card save: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during card save: " + e);
            } finally {
                // Reset auto-commit to its default state
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return success;
    }


    /**
     * Finds a card in the database by its ID.
     *
     * @param id The unique identifier of the card.
     * @return An Optional containing the Card if found, or an empty Optional if not found.
     * @throws HttpStatusException If there is an error during the search or a database connection issue.
     */
    @Override
    public Optional<Card> findCardById(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(FIND_CARD_BY_ID_SQL)) {

            findCardStmt.setString(1, id); // Set the card ID in the SQL query

            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    // Convert the ResultSet to a Card object
                    Card card = convertResultSetToCard(resultSet);
                    return Optional.of(card);
                }
            } catch (SQLException e) {
                System.out.println("Error finding card by ID: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding card by ID: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return Optional.empty();
    }


    /**
     * Retrieves all cards owned by a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return An array of Card objects owned by the user.
     * @throws HttpStatusException If there is an error during the retrieval or a database connection issue.
     */
    @Override
    public Card[] getUserCards(String userId) {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findUserCardsStmt = connection.prepareStatement(FIND_CARDS_OF_USER_SQL)) {

            findUserCardsStmt.setString(1, userId); // Set the user ID in the SQL query

            try (ResultSet resultSet = findUserCardsStmt.executeQuery()) {
                while (resultSet.next()) {
                    // Convert each ResultSet entry to a Card object and add to the list
                    Card card = convertResultSetToCard(resultSet);
                    cards.add(card);
                }
            } catch (
                    SQLException e) {
                System.out.println("Error finding cards of user: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding cards of user: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
// Convert the list of cards to an array and return it
        return cards.toArray(new Card[0]);
    }

    /**
     * Retrieves all cards in a specific user's deck.
     *
     * @param userId The unique identifier of the user.
     * @return An array of Card objects in the user's deck.
     * @throws HttpStatusException If there is an error during the retrieval or a database connection issue.
     */
    @Override
    public Card[] getUserDeckCards(String userId) {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findUserCardsStmt = connection.prepareStatement(FIND_CARDS_OF_USER_DECK_SQL)) {

            findUserCardsStmt.setString(1, userId); // Set the user ID in the SQL query

            try (ResultSet resultSet = findUserCardsStmt.executeQuery()) {
                while (resultSet.next()) {
                    // Convert each ResultSet entry to a Card object and add to the list
                    Card card = convertResultSetToCard(resultSet);
                    cards.add(card);
                }
            } catch (SQLException e) {
                System.out.println("Error finding cards of user: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding cards of user: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }

        // Convert the list of cards to an array and return it
        return cards.toArray(new Card[0]);
    }

    /**
     * Checks whether a specific card is in a user's stack.
     *
     * @param userId The unique identifier of the user.
     * @param cardId The unique identifier of the card.
     * @return True if the card is in the user's stack, false otherwise.
     * @throws HttpStatusException If there is an error during the check or a database connection issue.
     */
    @Override
    public boolean isCardInStack(String userId, String cardId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_CARD_IN_STACK_SQL)) {

            // Set the user ID and card ID in the SQL query
            stmt.setString(1, userId);
            stmt.setString(2, cardId);

            // Execute the query and process the result set
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // The query returns a count. If it's greater than 0, the card is in the user's stack
                    return resultSet.getInt(1) > 0;
                }
            } catch (SQLException e) {
                // Handle SQL exceptions during query execution
                System.out.println("Error checking if card is in stack: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if card is in stack: " + e);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false; // Return false if the card is not found
    }

    /**
     * Adds a card to a user's deck in the database.
     *
     * @param userId The unique identifier of the user.
     * @param cardId The unique identifier of the card to be added.
     * @return True if the card is successfully added to the deck, false otherwise.
     * @throws HttpStatusException If there is an error during the operation or a database connection issue.
     */
    @Override
    public boolean addCardToDeck(String userId, String cardId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to add the card to the user's deck
            try (PreparedStatement connectStmt = connection.prepareStatement(ADD_CARDS_TO_DECK_SQL)) {
                connectStmt.setBoolean(1, true); // Assuming true indicates the card is in the deck
                connectStmt.setString(2, cardId);
                connectStmt.setString(3, userId);

                // Execute the update and check the affected rows
                int affectedRows = connectStmt.executeUpdate();
                if (affectedRows > 0) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error connecting card to deck: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error connecting card to deck: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    /**
     * Resets (clears) a user's deck in the database.
     *
     * @param userId The unique identifier of the user whose deck is to be reset.
     * @return True if the deck is successfully reset, false otherwise.
     * @throws HttpStatusException If there is an error during the operation or a database connection issue.
     */
    @Override
    public boolean resetDeck(String userId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to reset the user's deck
            try (PreparedStatement stmt = connection.prepareStatement(RESET_USER_DECK_SQL)) {
                stmt.setBoolean(1, false); // Set the flag indicating the card is not in the deck
                stmt.setString(2, userId);

                // Execute the update and check the affected rows
                int affectedRows = stmt.executeUpdate();
                if (affectedRows >= 0) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error resetting user's deck: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error resetting user's deck: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    /**
     * Checks if a specific card is in a user's deck.
     *
     * @param userId The unique identifier of the user.
     * @param cardId The unique identifier of the card.
     * @return True if the card is in the user's deck, false otherwise.
     * @throws HttpStatusException If there is an error during the check or a database connection issue.
     */
    @Override
    public boolean isCardInDeck(String userId, String cardId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_CARD_IN_DECK_SQL)) {

            stmt.setString(1, userId); // Set the user ID in the SQL query
            stmt.setString(2, cardId); // Set the card ID in the SQL query

            // Execute the query and process the result set
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // Check if the count is greater than 0, indicating the card is in the deck
                    return resultSet.getInt(1) > 0;
                }
            } catch (SQLException e) {
                // Handle SQL exceptions during query execution
                System.out.println("Error checking if card is in deck: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if card is in deck: " + e);
            }
        } catch (SQLException e) {
            // Handle SQL exceptions related to database connectivity
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false; // Return false if the card is not found in the deck
    }


    /**
     * Deletes a card from a user's stack.
     *
     * @param userId The unique identifier of the user.
     * @param cardId The unique identifier of the card to be deleted.
     * @return True if the card is successfully deleted from the stack, false otherwise.
     * @throws HttpStatusException If there is an error during the operation or a database connection issue.
     */
    @Override
    public boolean deleteCardFromStack(String userId, String cardId) {
        try (Connection connection = database.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);

            // Prepare and execute the SQL statement to delete the card from the user's stack
            try (PreparedStatement deleteStmt = connection.prepareStatement(DELETE_CARD_FROM_STACK_SQL)) {
                deleteStmt.setString(1, userId);
                deleteStmt.setString(2, cardId);

                int affectedRows = deleteStmt.executeUpdate();
                if (affectedRows > 0) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error deleting card from user stack: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting card from user stack: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    /**
     * Adds a card to a user's stack.
     *
     * @param userId The unique identifier of the user.
     * @param cardId The unique identifier of the card to be added.
     * @return True if the card is successfully added to the stack, false otherwise.
     * @throws HttpStatusException If there is a database connection error.
     */
    @Override
    public boolean addCardToStack(String userId, String cardId) {
        try (Connection connection = database.getConnection();
             PreparedStatement addStmt = connection.prepareStatement(ADD_CARD_TO_STACK_SQL)) {

            addStmt.setString(1, userId);
            addStmt.setString(2, cardId);

            int affectedRows = addStmt.executeUpdate();
            return affectedRows > 0; // Return true if the card is successfully added
        } catch (SQLException e) {
            System.out.println("Error saving card to user stack: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    /**
     * Finds a card that is not possessed by any user.
     *
     * @return An Optional containing the Card if found, or an empty Optional if no such card is found.
     * @throws HttpStatusException If there is an error during the search or a database connection issue.
     */
    @Override
    public Optional<Card> getCardNotPossesed() {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(GET_CARD_NON_POSSESSED_SQL)) {

            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    Card card = convertResultSetToCard(resultSet); // Convert ResultSet to a Card object
                    return Optional.of(card);
                }
            } catch (SQLException e) {
                System.out.println("Error finding card non possesed card: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding non possesed card: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }

        return Optional.empty(); // Return an empty Optional if no such card is found
    }


    private Card convertResultSetToCard(ResultSet resultSet) throws SQLException {
        Card card = new Card();
        card.setId(resultSet.getString("card_id"));
        card.setName(resultSet.getString("name"));
        card.setDamage(resultSet.getInt("damage"));
        card.setElementType(resultSet.getString("elementtype"));
        card.setCardType(resultSet.getString("cardtype"));
        return card;
    }


}
