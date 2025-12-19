package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao {
    // constructor injects the datasource and passes it to the base dao
    public MySqlProductDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String subCategory) {
        // initialize list to hold products
        List<Product> products = new ArrayList<>();
        // parameterized SQL to prevent SQL injection
        String sql = """
                SELECT
                    *
                FROM
                    products
                WHERE
                    (category_id = ? OR ? = -1)
                    AND (price >= ? OR ? = -1)
                    AND (price <= ? OR ? = -1)
                    AND (subcategory = ? OR ? = '');""";
        // set defaults if parameters are null
        categoryId = categoryId == null ? -1 : categoryId;
        minPrice = minPrice == null ? new BigDecimal("-1") : minPrice;
        maxPrice = maxPrice == null ? new BigDecimal("-1") : maxPrice;
        subCategory = subCategory == null ? "" : subCategory;

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            statement.setInt(1, categoryId);
            statement.setInt(2, categoryId);
            statement.setBigDecimal(3, minPrice);
            statement.setBigDecimal(4, minPrice);
            statement.setBigDecimal(5, maxPrice);
            statement.setBigDecimal(6, maxPrice);
            statement.setString(7, subCategory);
            statement.setString(8, subCategory);
            // execute query
            ResultSet row = statement.executeQuery();
            // map each result row to a product object
            while (row.next()) {
                Product product = mapProductRow(row);
                products.add(product);
            }
        }
        catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
        return products; // return list of products
    }

    @Override
    public List<Product> listByCategoryId(int categoryId) {
        // initialize list to hold products
        List<Product> products = new ArrayList<>();
        // parameterized SQL to prevent SQL injection
        String sql = """
                    SELECT
                        *
                    FROM
                        products
                    WHERE
                        category_id = ?;""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            statement.setInt(1, categoryId);
            // execute query
            ResultSet row = statement.executeQuery();
            // map each result row to a product object
            while (row.next()) {
                Product product = mapProductRow(row);
                products.add(product);
            }
        } catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
        return products; // return list of products
    }

    @Override
    public Product getById(int productId) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                SELECT
                    *
                FROM
                    products
                WHERE
                    product_id = ?;""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            statement.setInt(1, productId);
            // execute query
            ResultSet row = statement.executeQuery();
            // return product if found
            if (row.next()) {
                return mapProductRow(row);
            }
        } catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
        return null; // return null if not found
    }

    @Override
    public Product create(Product product) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                INSERT INTO
                    products(name, price, category_id, description, subcategory, image_url, stock, featured)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?);""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // bind parameters for prepared statement
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            // execute update
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the generated keys
                ResultSet generatedKeys = statement.getGeneratedKeys();
                // check if keys are generated corrrectly
                if (generatedKeys.next()) {
                    // Retrieve the auto-incremented ID
                    int orderId = generatedKeys.getInt(1);
                    // get the newly inserted category
                    return getById(orderId);
                }
            }
        } catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
        return null; // return null if insert failed
    }

    @Override
    public void update(int productId, Product product) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                UPDATE
                    products
                SET
                    name = ?,
                    price = ?,
                    category_id = ?,
                    description = ?,
                    subcategory = ?,
                    image_url = ?,
                    stock = ?,
                    featured = ?
                WHERE
                    product_id = ?;""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getSubCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);
            // execute update
            statement.executeUpdate();
        }
        catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                DELETE FROM
                    products
                WHERE
                    product_id = ?;""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind parameter for prepared statement
            statement.setInt(1, productId);
            // execute update
            statement.executeUpdate();
        }
        catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
    }

    protected static Product mapProductRow(ResultSet row) throws SQLException {
        // map result set row to product object
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");
        // create and return product based on parameters
        return new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
    }
}
