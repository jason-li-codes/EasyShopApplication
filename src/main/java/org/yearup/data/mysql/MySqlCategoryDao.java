package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {

    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {

        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT
                    *
                FROM
                    categories
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet row = statement.executeQuery();

            while (row.next()) {
                Category category = mapCategoryRow(row);
                categories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId) {

        for (Category category : getAllCategories()) {
            if (category.getCategoryId() == categoryId) {
                return category;
            }
        }
        return null;
    }

    @Override
    public Category create(Category category) {

        String sql = """
                INSERT INTO
                    categories (name, description)
                VALUES
                    (?, ?)""";

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {

                ResultSet generatedKeys = statement.getGeneratedKeys();

                if (generatedKeys.next()) {
                    int categoryId = generatedKeys.getInt(1);
                    return getById(categoryId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void update(int categoryId, Category category) {

        String sql = """
                UPDATE
                    categories
                SET
                    name = ?,
                    description = ?
                WHERE
                    categoryId = ?""";

        try (Connection connection = getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int categoryId) {

        String sql = """
                DELETE FROM
                    categories
                WHERE
                    categoryId = ?""";

        try (Connection connection = getConnection()) {

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            statement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Category mapCategoryRow(ResultSet row) throws SQLException {

        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        return new Category(categoryId, name, description);
    }

}
