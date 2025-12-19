package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL implementation of the ShoppingCartDao interface.
 * This class handles database operations related to a user's shopping cart,
 * including retrieving cart contents, adding items, updating quantities,
 * and removing items. It uses JDBC with parameterized SQL statements to
 * ensure secure database access. The class is managed by Spring and relies
 * on a shared MySQL DAO base for connection handling.
 */
@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    // constructor to initialize the data source
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Retrieves the current shopping cart for a specific user.
     * This method queries the shopping_cart table and joins product data
     * to fully populate each ShoppingCartItem with product details and quantities.
     * Each row in the result set is converted into a ShoppingCartItem and added
     * to a ShoppingCart object. If the user has no items, an empty shopping cart
     * is returned.
     */
    @Override
    public ShoppingCart getByUserId(int userId) {
        // instantiate new empty ShoppingCart object
        ShoppingCart shoppingCart = new ShoppingCart();
        // parameterized SQL to prevent SQL injection
        String sql = """
                SELECT
                    *
                FROM
                    shopping_cart sc
                    JOIN products p on (sc.product_id = p.product_id)
                WHERE
                    user_id = ?""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind userId parameter for prepared statement
            statement.setInt(1, userId);
            // execute query
            ResultSet row = statement.executeQuery();
            // iterate through each row in the result set
            while (row.next()) {
                // retrieve product and quantity details
                int productId = row.getInt("product_id");
                String name = row.getString("name");
                BigDecimal price = row.getBigDecimal("price");
                int categoryId = row.getInt("category_id");
                String description = row.getString("description");
                String subCategory = row.getString("subcategory");
                int stock = row.getInt("stock");
                boolean isFeatured = row.getBoolean("featured");
                String imageUrl = row.getString("image_url");
                int quantity = row.getInt("quantity");
                // create ShoppingCartItem object
                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl));
                item.setQuantity(quantity);
                // add item to shopping cart
                shoppingCart.add(item);
            }
            // return populated ShoppingCart object
            return shoppingCart;
        } catch (SQLException e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a shopping cart representation based on a completed order.
     * This method queries order line items and joins related order and product
     * data to rebuild the contents of the cart associated with the given order.
     * Each product and quantity is mapped into a ShoppingCartItem and added
     * to a ShoppingCart object. This is commonly used for viewing past orders.
     */
    @Override
    public ShoppingCart getByOrderId(int orderId) {
        // instantiate new empty ShoppingCart object
        ShoppingCart shoppingCart = new ShoppingCart();
        // parameterized SQL to prevent SQL injection
        String sql = """
                SELECT
                    *
                FROM
                    order_line_items oli
                    JOIN orders o on (oli.order_id = o.order_id)
                    JOIN products p on (oli.product_id = p.product_id)
                WHERE
                    order_id = ?""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind orderId parameter for prepared statement
            statement.setInt(1, orderId);
            // execute query
            ResultSet row = statement.executeQuery();
            // iterate through each row in the result set
            while (row.next()) {
                // retrieve product and quantity details
                int productId = row.getInt("product_id");
                String name = row.getString("name");
                BigDecimal price = row.getBigDecimal("price");
                int categoryId = row.getInt("category_id");
                String description = row.getString("description");
                String subCategory = row.getString("subcategory");
                int stock = row.getInt("stock");
                boolean isFeatured = row.getBoolean("featured");
                String imageUrl = row.getString("image_url");
                int quantity = row.getInt("quantity");
                // create ShoppingCartItem object
                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl));
                item.setQuantity(quantity);
                // add item to shopping cart
                shoppingCart.add(item);
            }
            // return populated ShoppingCart object
            return shoppingCart;
        } catch (SQLException e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a product to a user's shopping cart.
     * If the product already exists in the cart, its quantity is incremented
     * by one using a database-level duplicate key update. This method ensures
     * that cart state remains consistent without requiring separate checks.
     * After the update, the refreshed ShoppingCart is retrieved and returned.
     */
    @Override
    public ShoppingCart addItem(int userId, ShoppingCartItem item) {
        // parameterized SQL to add new item or increment quantity if it already exists
        String sql = """
                INSERT INTO
                    shopping_cart (user_id, product_id, quantity)
                VALUES
                    (?, ?, 1)
                ON DUPLICATE KEY UPDATE
                    quantity = quantity + 1;""";

        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, item.getProduct().getProductId());
            // execute update
            preparedStatement.executeUpdate();
            // return updated shopping cart
            return getByUserId(userId);
        } catch (Exception e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the quantity of a specific item in a user's shopping cart.
     * This method executes a parameterized update query using the user ID
     * and product ID to identify the correct cart item. If no rows are affected,
     * the update is considered unsuccessful and an exception is thrown.
     * This method does not return a value after completion.
     */
    @Override
    public void updateItem(int userId, ShoppingCartItem item) {
        // parameterized SQL to update the quantity of an item in the shopping cart
        String sql = """
                UPDATE
                    shopping_cart
                SET
                    quantity = ?
                WHERE
                    user_id = ?
                    AND product_id = ?""";

        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            preparedStatement.setInt(1, item.getQuantity());
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3,  item.getProduct().getProductId());
            // execute update
            int rows = preparedStatement.executeUpdate();
            // check if update affected any rows
            if (rows == 0) throw new SQLException("Update failed, no rows affected!");

        } catch (Exception e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes all items from a user's shopping cart.
     * This method removes every cart entry associated with the specified user ID
     * from the shopping_cart table. It is typically used after checkout or when
     * a user explicitly clears their cart. The operation completes without
     * returning any data.
     */
    @Override
    public void deleteShoppingCart(int userId) {
        // parameterized SQL to delete all items for a given user
        String sql = """
                DELETE FROM
                    shopping_cart
                WHERE
                    user_id = ?""";

        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // bind userId parameter for prepared statement
            preparedStatement.setInt(1, userId);
            // execute deletion
            preparedStatement.executeUpdate();

        } catch (Exception e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the total monetary value of a shopping cart.
     * This method delegates the calculation to the ShoppingCart model,
     * which sums the price of each product multiplied by its quantity.
     * It provides a simple way to retrieve the cart total without
     * performing additional database queries.
     */
    @Override
    public BigDecimal getShoppingCartTotal(ShoppingCart shoppingCart) {
        // return total value of items in the shopping cart
        return shoppingCart.getTotal();
    }

}
