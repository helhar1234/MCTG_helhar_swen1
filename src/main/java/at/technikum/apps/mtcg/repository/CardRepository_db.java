package at.technikum.apps.mtcg.repository;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;
import at.technikum.apps.mtcg.entity.PackageCard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardRepository_db implements CardRepository{
    private final Database database = new Database();

    private final String SAVE_PACKAGE_SQL = "INSERT INTO packages (package_id) VALUES (?) RETURNING package_id";
    private final String SAVE_CARD_SQL = "INSERT INTO cards (card_id, name, damage, elementtype, cardtype) VALUES (?,?,?,?,?) RETURNING card_id";
    private final String CONNECT_CARDS_PACKAGES_SQL = "INSERT INTO cards_packages (card_fk, package_fk) VALUES (?,?)";
    private final String FIND_CARD_BY_ID_SQL = "SELECT * FROM cards WHERE card_id = ?";
    private final String FIND_PACKAGE_BY_ID_SQL = "SELECT * FROM packages WHERE package_id = ?";
    private final String FIND_CARDS_IN_PACKAGE_SQL = "SELECT c.* FROM cards c JOIN cards_packages cp ON c.card_id = cp.card_fk WHERE package_fk = ?";
    private final String FIND_CARDS_OF_USER_SQL = "SELECT c.* FROM cards c JOIN user_cards uc ON c.card_id = uc.card_fk WHERE user_fk = ?";
    private final String FIND_CARDS_OF_USER_DECK_SQL = "SELECT c.* FROM cards c JOIN user_cards uc ON c.card_id = uc.card_fk WHERE user_fk = ? AND uc.indeck is true";
    private final String CHECK_CARD_IN_STACK_SQL = "SELECT COUNT(*) FROM user_cards WHERE user_fk = ? AND card_fk = ?";
    private final String ADD_CARDS_TO_DECK_SQL = "UPDATE user_cards SET indeck = ? WHERE card_fk = ? AND user_fk = ?";
    private final String RESET_USER_DECK_SQL = "UPDATE user_cards SET indeck = ? WHERE user_fk = ?";
    @Override
    public boolean savePackage(String id) {
        boolean success = false;

        try (Connection connection = database.getConnection();
             PreparedStatement savePackageStatement = connection.prepareStatement(SAVE_PACKAGE_SQL)) {

            savePackageStatement.setString(1, id);

            // Execute the query and get the result set
            try (ResultSet resultSet = savePackageStatement.executeQuery()) {
                // If resultSet has an entry, the insert was successful
                if (resultSet.next()) {
                    String returnedId = resultSet.getString("package_id");
                    success = returnedId != null && !returnedId.isEmpty();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during package save: " + e.getMessage());
        }
        return success;
    }


    @Override
    public boolean saveCard(PackageCard card) {
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
        }
        return success;
    }

    @Override
    public boolean addCardToPackage(String packageId, String cardId) {
        try (Connection connection = database.getConnection();
             PreparedStatement connectStmt = connection.prepareStatement(CONNECT_CARDS_PACKAGES_SQL)) {

            connectStmt.setString(1, cardId);
            connectStmt.setString(2, packageId);

            // Execute the insert statement using executeUpdate
            int affectedRows = connectStmt.executeUpdate();

            // Check if the insert was successful based on affected rows
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Error connecting card to package: " + e.getMessage());
            return false;
        }
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
            }
        } catch (SQLException e) {
            System.out.println("Error finding card by ID: " + e.getMessage());
            // Optionally, handle or log the exception as appropriate for your application
        }
        return Optional.empty();
    }

    @Override
    public Optional<Package> findPackageById(String id) {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(FIND_PACKAGE_BY_ID_SQL)) {

            findCardStmt.setString(1, id);

            try (ResultSet resultSet = findCardStmt.executeQuery()) {
                if (resultSet.next()) {
                    // Assuming you have a method to convert ResultSet to a Card object
                    Package aPackage = convertResultSetToPackage(resultSet);
                    return Optional.of(aPackage);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding package by ID: " + e.getMessage());
            // Optionally, handle or log the exception as appropriate for your application
        }
        return Optional.empty();
    }

    @Override
    public Card[] getPackageCardsById(String packageId) {
        List<Card> cards = new ArrayList<>();

        try (Connection connection = database.getConnection();
             PreparedStatement findCardsStmt = connection.prepareStatement(FIND_CARDS_IN_PACKAGE_SQL)) {

            findCardsStmt.setString(1, packageId);

            try (ResultSet resultSet = findCardsStmt.executeQuery()) {
                while (resultSet.next()) {
                    Card card = convertResultSetToCard(resultSet);
                    cards.add(card);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding cards in package: " + e.getMessage());
            // Optionally, handle or log the exception as appropriate for your application
        }

        return cards.toArray(new Card[0]);
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
            }
        } catch (SQLException e) {
            System.out.println("Error finding cards of user: " + e.getMessage());
            // Optionally, handle or log the exception as appropriate for your application
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
            }
        } catch (SQLException e) {
            System.out.println("Error finding cards of user: " + e.getMessage());
            // Optionally, handle or log the exception as appropriate for your application
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
            }
        } catch (SQLException e) {
            System.out.println("Error checking if card is in stack: " + e.getMessage());
            // Optionally, handle or log the exception as appropriate for your application
        }
        return false;
    }

    @Override
    public boolean addCardToDeck(String userId, String cardId) {
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
            return false;
        }
    }

    @Override
    public boolean resetDeck(String userId) {
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
            // Optionally, handle or log the exception as appropriate for your application
            return false;
        }
    }



    private Package convertResultSetToPackage(ResultSet resultSet) throws SQLException {
        Package aPackage = new Package();
        aPackage.setId(resultSet.getString("package_id"));
        aPackage.setPrice(resultSet.getInt("price"));
        return aPackage;
    }

    private Card convertResultSetToCard(ResultSet resultSet) throws SQLException {
        // Implement this method to convert a ResultSet to a Card object
        // Extract values from the resultSet and populate a new Card object
        Card card = new Card();
        card.setId(resultSet.getString("card_id"));
        card.setName(resultSet.getString("name"));
        card.setDamage(resultSet.getInt("damage"));
        card.setElementType(resultSet.getString("elementtype"));
        card.setCardType(resultSet.getString("cardtype"));
        return card;
    }



}
