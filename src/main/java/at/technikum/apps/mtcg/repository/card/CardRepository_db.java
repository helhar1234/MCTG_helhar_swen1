package at.technikum.apps.mtcg.repository.card;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.PackageCard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardRepository_db implements CardRepository {
    // DB CONNECTION
    private final Database database = new Database();

    // SQL STATEMENTS
    private final String SAVE_CARD_SQL = "INSERT INTO cards (card_id, name, damage, elementtype, cardtype) VALUES (?,?,?,?,?) RETURNING card_id";
    private final String FIND_CARD_BY_ID_SQL = "SELECT * FROM cards WHERE card_id = ?";
    private final String FIND_CARDS_OF_USER_SQL = "SELECT c.* FROM cards c JOIN user_cards uc ON c.card_id = uc.card_fk WHERE user_fk = ?";
    private final String FIND_CARDS_OF_USER_DECK_SQL = "SELECT c.* FROM cards c JOIN user_cards uc ON c.card_id = uc.card_fk WHERE user_fk = ? AND uc.indeck is true";
    private final String CHECK_CARD_IN_STACK_SQL = "SELECT COUNT(*) FROM user_cards WHERE user_fk = ? AND card_fk = ?";
    private final String CHECK_CARD_IN_DECK_SQL = "SELECT COUNT(*) FROM user_cards WHERE user_fk = ? AND card_fk = ? AND indeck is true";
    private final String ADD_CARDS_TO_DECK_SQL = "UPDATE user_cards SET indeck = ? WHERE card_fk = ? AND user_fk = ?";
    private final String RESET_USER_DECK_SQL = "UPDATE user_cards SET indeck = ? WHERE user_fk = ?";
    private final String DELETE_CARD_FROM_STACK_SQL = "DELETE FROM user_cards WHERE user_fk = ? AND card_fk = ?";
    private final String ADD_CARD_TO_STACK_SQL = "INSERT INTO user_cards (user_fk, card_fk) VALUES (?, ?)";

    // IMPLEMENTATIONS

    @Override
    public boolean saveCard(PackageCard card) throws SQLException {
        boolean success = false;

        // Determine elementtype based on card name
        String elementtype;
        if (card.getName().toLowerCase().contains("water")) {
            elementtype = "water";
        } else if (card.getName().toLowerCase().contains("fire")) {
            elementtype = "fire";
        } else {
            elementtype = "normal";
        }

        // Determine cardtype based on card name
        String cardtype = card.getName().toLowerCase().contains("spell") ? "spell" : "monster";

        // Now, use the SAVE_CARD_SQL statement to insert the card into the database
        try (Connection connection = database.getConnection();
             PreparedStatement saveCardStatement = connection.prepareStatement(SAVE_CARD_SQL)) {

            saveCardStatement.setString(1, card.getId());
            saveCardStatement.setString(2, card.getName());
            saveCardStatement.setDouble(3, card.getDamage());
            saveCardStatement.setString(4, elementtype);
            saveCardStatement.setString(5, cardtype);

            // Execute the query and get the result set
            try (ResultSet resultSet = saveCardStatement.executeQuery()) {
                // If resultSet has an entry, the insert was successful
                if (resultSet.next()) {
                    String returnedId = resultSet.getString("card_id");
                    success = returnedId != null && !returnedId.isEmpty();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during card save: " + e.getMessage());
            throw new SQLException(e);
        }
        return success;
    }

    @Override
    public Optional<Card> findCardById(String id) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(FIND_CARD_BY_ID_SQL)) {

            findCardStmt.setString(1, id);

            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a Card object
                    Card card = convertResultSetToCard(resultSet);
                    return Optional.of(card);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding card by ID: " + e.getMessage());
            throw new SQLException(e);
        }
        return Optional.empty();
    }


    @Override
    public Card[] getUserCards(String userId) throws SQLException {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findUserCardsStmt = connection.prepareStatement(FIND_CARDS_OF_USER_SQL)) {

            findUserCardsStmt.setString(1, userId);

            try (ResultSet resultSet = findUserCardsStmt.executeQuery()) {
                while (resultSet.next()) {
                    Card card = convertResultSetToCard(resultSet);
                    cards.add(card);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding cards of user: " + e.getMessage());
            throw new SQLException(e);
        }

        return cards.toArray(new Card[0]);
    }

    @Override
    public Card[] getUserDeckCards(String userId) throws SQLException {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findUserCardsStmt = connection.prepareStatement(FIND_CARDS_OF_USER_DECK_SQL)) {

            findUserCardsStmt.setString(1, userId);

            try (ResultSet resultSet = findUserCardsStmt.executeQuery()) {
                while (resultSet.next()) {
                    Card card = convertResultSetToCard(resultSet);
                    cards.add(card);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding cards of user: " + e.getMessage());
            throw new SQLException(e);
        }

        return cards.toArray(new Card[0]);
    }

    @Override
    public boolean isCardInStack(String userId, String cardId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_CARD_IN_STACK_SQL)) {

            stmt.setString(1, userId);
            stmt.setString(2, cardId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // If the count is greater than 0, the card is in the stack
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking if card is in stack: " + e.getMessage());
            throw new SQLException(e);
        }
        return false;
    }

    @Override
    public boolean addCardToDeck(String userId, String cardId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement connectStmt = connection.prepareStatement(ADD_CARDS_TO_DECK_SQL)) {

            connectStmt.setBoolean(1, true);
            connectStmt.setString(2, cardId);
            connectStmt.setString(3, userId);

            // Execute the insert statement using executeUpdate
            int affectedRows = connectStmt.executeUpdate();

            // Check if the insert was successful based on affected rows
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error connecting card to deck: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public boolean resetDeck(String userId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(RESET_USER_DECK_SQL)) {

            stmt.setBoolean(1, false); // Assuming 'false' represents that the card is not in the deck
            stmt.setString(2, userId);

            // Execute the update statement
            int affectedRows = stmt.executeUpdate();

            // The operation is considered successful if one or more rows are affected or none are affected but the user has no deck cards
            return affectedRows >= 0;
        } catch (SQLException e) {
            System.out.println("Error resetting user's deck: " + e.getMessage());
            throw new SQLException(e);
        }
    }


    @Override
    public boolean isCardInDeck(String userId, String cardId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_CARD_IN_DECK_SQL)) {

            stmt.setString(1, userId);
            stmt.setString(2, cardId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    // If the count is greater than 0, the card is in the stack
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking if card is in stack: " + e.getMessage());
            throw new SQLException(e);
        }
        return false;
    }

    @Override
    public boolean deleteCardFromStack(String userId, String cardId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement(DELETE_CARD_FROM_STACK_SQL)) {

            deleteStmt.setString(1, userId);
            deleteStmt.setString(2, cardId);


            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting card from user stack: " + e.getMessage());
            throw new SQLException(e);
        }
    }

    @Override
    public boolean addCardToStack(String userId, String cardId) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement addStmt = connection.prepareStatement(ADD_CARD_TO_STACK_SQL)) {

            addStmt.setString(1, userId);
            addStmt.setString(2, cardId);


            int affectedRows = addStmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error saving card to user stack: " + e.getMessage());
            throw new SQLException(e);
        }
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
