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

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {

        ShoppingCart shoppingCart = new ShoppingCart();

        String sql = """
                SELECT
                    *
                FROM
                    shopping_cart sc
                    JOIN products p on (sc.product_id = p.product_id)
                WHERE
                    user_id = ?""";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {

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

                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl));
                item.setQuantity(quantity);
                shoppingCart.add(item);
            }
            return shoppingCart;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShoppingCart getByOrderId(int orderId) {

        ShoppingCart shoppingCart = new ShoppingCart();

        String sql = """
                SELECT
                    *
                FROM
                    order_line_items oli
                    JOIN orders o on (oli.order_id = o.order_id)
                    JOIN products p on (oli.product_id = p.product_id)
                WHERE
                    order_id = ?""";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, orderId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {

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

                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl));
                item.setQuantity(quantity);
                shoppingCart.add(item);
            }
            return shoppingCart;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShoppingCart addItemShoppingCart(int userId, ShoppingCartItem item) {

        String sql = """
                INSERT INTO
                    shopping_cart (user_id, product_id, quantity)
                VALUES
                    (?, ?, 1)
                ON DUPLICATE KEY UPDATE
                    quantity = quantity + 1;""";

        try (Connection connection = getConnection()) {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, item.getProduct().getProductId());

            preparedStatement.executeUpdate();
            return getByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ShoppingCart updateItemShoppingCart(int userId, ShoppingCartItem item) {

        String sql = """
                UPDATE
                    shopping_cart
                SET
                    quantity = ?
                WHERE
                    user_id = ?
                    AND product_id = ?""";

        try (Connection connection = getConnection()) {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, item.getQuantity());
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3,  item.getProduct().getProductId());

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) throw new SQLException("Update failed, no rows affected!");
            return getByUserId(userId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteShoppingCart(int userId) {

        String sql = """
                DELETE FROM
                    shopping_cart
                WHERE
                    user_id = ?""";
        try (Connection connection = getConnection()) {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);

            int rows = preparedStatement.executeUpdate();
            if (rows == 0) throw new SQLException("Update failed, no rows affected!");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BigDecimal getShoppingCartTotal(ShoppingCart shoppingCart) {
        return shoppingCart.getTotal();
    }

}
