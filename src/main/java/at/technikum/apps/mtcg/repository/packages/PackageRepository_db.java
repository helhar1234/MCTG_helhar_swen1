package at.technikum.apps.mtcg.repository.packages;

import at.technikum.apps.mtcg.database.Database;
import at.technikum.apps.mtcg.entity.Card;
import at.technikum.apps.mtcg.entity.Package;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PackageRepository_db implements PackageRepository {
    // DB CONNECTION
    private final Database database = new Database();

    // SQL STATEMENTS
    private final String SAVE_PACKAGE_SQL = "INSERT INTO packages (package_id) VALUES (?) RETURNING package_id";
    private final String CONNECT_CARDS_PACKAGES_SQL = "INSERT INTO cards_packages (card_fk, package_fk) VALUES (?,?)";
    private final String FIND_PACKAGE_BY_ID_SQL = "SELECT * FROM packages WHERE package_id = ?";
    private final String FIND_AVAILABLE_PACKAGE_BY_ID_SQL = "SELECT * FROM packages WHERE package_id = ? AND sold is false";
    private final String GET_FIRST_PACKAGE_NOT_POSSESSING_SQL = "SELECT p.package_id FROM packages p WHERE NOT EXISTS (SELECT 1 FROM cards_packages cp JOIN user_cards uc ON cp.card_fk = uc.card_fk WHERE cp.package_fk = p.package_id AND uc.user_fk = ?) AND p.sold = false ORDER BY p.orderid ASC LIMIT 1";
    private final String FIND_CARDS_IN_PACKAGE_SQL = "SELECT c.* FROM cards c JOIN cards_packages cp ON c.card_id = cp.card_fk WHERE package_fk = ?";
    private final String UPDATE_PACKAGE_SOLD_SQL = "UPDATE packages SET sold = true WHERE package_id = ?";

    // IMPLEMENTATIONS
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
    public String getFirstPackageNotPossessing(String userId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_FIRST_PACKAGE_NOT_POSSESSING_SQL)) {

            stmt.setString(1, userId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("package_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error finding first package not possessing: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Optional<Package> getAvailablePackages(String packageId) {
        try (Connection connection = database.getConnection();
             PreparedStatement findCardStmt = connection.prepareStatement(FIND_AVAILABLE_PACKAGE_BY_ID_SQL)) {

            findCardStmt.setString(1, packageId);

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
    public boolean deletePackage(String packageId) {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_PACKAGE_SOLD_SQL)) {

            stmt.setString(1, packageId);

            // Execute update statement
            int affectedRows = stmt.executeUpdate();

            // Return true if the update was successful (one row updated)
            return affectedRows == 1;

        } catch (SQLException e) {
            System.out.println("Error updating package sold status: " + e.getMessage());
            return false;
        }
    }

    private Package convertResultSetToPackage(ResultSet resultSet) throws SQLException {
        Package aPackage = new Package();
        aPackage.setId(resultSet.getString("package_id"));
        aPackage.setPrice(resultSet.getInt("price"));
        aPackage.setSold(resultSet.getBoolean("sold"));
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
