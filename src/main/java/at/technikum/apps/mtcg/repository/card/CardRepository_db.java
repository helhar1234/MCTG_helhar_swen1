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

    public CardRepository_db(Database database){
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

    @Override
    public boolean saveCard(PackageCard card) {
        boolean success = false;

        // Determine elementtype and cardtype
        String elementtype = card.getName().toLowerCase().contains("water") ? "water" :
                card.getName().toLowerCase().contains("fire") ? "fire" :
                        "normal";
        String cardtype = card.getName().toLowerCase().contains("spell") ? "spell" : "monster";

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement saveCardStatement = connection.prepareStatement(SAVE_CARD_SQL)) {
                saveCardStatement.setString(1, card.getId());
                saveCardStatement.setString(2, card.getName());
                saveCardStatement.setDouble(3, card.getDamage());
                saveCardStatement.setString(4, elementtype);
                saveCardStatement.setString(5, cardtype);

                int affectedRows = saveCardStatement.executeUpdate();

                if (affectedRows == 1) {
                    connection.commit(); // Commit the transaction
                    success = true;
                } else {
                    connection.rollback(); // Rollback the transaction
                }
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction
                System.out.println("Error during card save: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error during card save: " + e);
            }
            connection.setAutoCommit(true); // Reset auto-commit to default
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return success;
    }


    @Override
    public Optional<Card> findCardById(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(FIND_CARD_BY_ID_SQL)) {

            findCardStmt.setString(1, id);

            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a Card object
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


    @Override
    public Card[] getUserCards(String userId) {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findUserCardsStmt = connection.prepareStatement(FIND_CARDS_OF_USER_SQL)) {

            findUserCardsStmt.setString(1, userId);

            try (ResultSet resultSet = findUserCardsStmt.executeQuery()) {
                while (resultSet.next()) {
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

        return cards.toArray(new Card[0]);
    }

    @Override
    public Card[] getUserDeckCards(String userId) {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findUserCardsStmt = connection.prepareStatement(FIND_CARDS_OF_USER_DECK_SQL)) {

            findUserCardsStmt.setString(1, userId);

            try (ResultSet resultSet = findUserCardsStmt.executeQuery()) {
                while (resultSet.next()) {
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

        return cards.toArray(new Card[0]);
    }

    @Override
    public boolean isCardInStack(String userId, String cardId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_CARD_IN_STACK_SQL)) {

            stmt.setString(1, userId);
            stmt.setString(2, cardId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // If the count is greater than 0, the card is in the stack
                    return resultSet.getInt(1) > 0;
                }
            } catch (SQLException e) {
                System.out.println("Error checking if card is in stack: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if card is in stack: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false;
    }

    @Override
    public boolean addCardToDeck(String userId, String cardId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement connectStmt = connection.prepareStatement(ADD_CARDS_TO_DECK_SQL)) {
                connectStmt.setBoolean(1, true);
                connectStmt.setString(2, cardId);
                connectStmt.setString(3, userId);

                int affectedRows = connectStmt.executeUpdate();

                if (affectedRows > 0) {
                    connection.commit(); // Commit the transaction
                    return true;
                } else {
                    connection.rollback(); // Rollback the transaction
                    return false;
                }
            } catch (SQLException e) {
                System.out.println("Error connecting card to deck: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error connecting card to deck: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }


    @Override
    public boolean resetDeck(String userId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmt = connection.prepareStatement(RESET_USER_DECK_SQL)) {
                stmt.setBoolean(1, false); // Assuming 'false' represents that the card is not in the deck
                stmt.setString(2, userId);

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


    @Override
    public boolean isCardInDeck(String userId, String cardId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_CARD_IN_DECK_SQL)) {

            stmt.setString(1, userId);
            stmt.setString(2, cardId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // If the count is greater than 0, the card is in the stack
                    return resultSet.getInt(1) > 0;
                }
            } catch (SQLException e) {
                System.out.println("Error checking if card is in stack: " + e.getMessage());
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if card is in stack: " + e);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
        return false;
    }

    @Override
    public boolean deleteCardFromStack(String userId, String cardId) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false); // Start transaction

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


    @Override
    public boolean addCardToStack(String userId, String cardId) {
        try (Connection connection = database.getConnection();
             PreparedStatement addStmt = connection.prepareStatement(ADD_CARD_TO_STACK_SQL)) {

            addStmt.setString(1, userId);
            addStmt.setString(2, cardId);

            int affectedRows = addStmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error saving card to user stack: " + e.getMessage());
            throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Database connection error: " + e);
        }
    }

    @Override
    public Optional<Card> getCardNotPossesed() {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(GET_CARD_NON_POSSESSED_SQL)) {

            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    Card card = convertResultSetToCard(resultSet);
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
        return Optional.empty();
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
