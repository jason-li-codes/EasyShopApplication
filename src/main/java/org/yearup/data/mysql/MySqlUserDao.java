package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.UserDao;
import org.yearup.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlUserDao extends MySqlDaoBase implements UserDao {
    // constructor to initialize the data source
    @Autowired
    public MySqlUserDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public User create(User newUser) {
        // parameterized SQL to insert a new user into the database
        String sql = """
                INSERT INTO
                    users (username, hashed_password, role)
                VALUES
                    (?, ?, ?)""";
        // hash the password using BCrypt for security
        String hashedPassword = new BCryptPasswordEncoder().encode(newUser.getPassword());

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // bind parameters for prepared statement
            ps.setString(1, newUser.getUsername());
            ps.setString(2, hashedPassword);
            ps.setString(3, newUser.getRole());

            // execute update
            int rowsAffected = ps.executeUpdate();
            // retrieve auto-generated ID if insert was successful
            if (rowsAffected > 0) {
                // Retrieve the generated keys
                ResultSet generatedKeys = ps.getGeneratedKeys();

                if (generatedKeys.next()) {
                    // Retrieve the auto-incremented ID
                    int userId = generatedKeys.getInt(1);
                    newUser.setId(userId);
                }
            }
            // retrieve the user from DB to ensure full object is returned
            User user = getUserByUserName(newUser.getUsername());
            // clear password before returning
            user.setPassword("");
            // return newly created user object
            return user;
        }
        catch (SQLException e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public User update(User updatedUser) {
        // parameterized SQL to update an existing user
        String sql = """
                UPDATE
                    users
                SET
                    username = ?,
                    hashed_password = ?,
                    role = ?
                WHERE
                    user_id = ?""";
        // hash the updated password
        String updatedHashedPassword = new BCryptPasswordEncoder().encode(updatedUser.getPassword());

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            ps.setString(1, updatedUser.getUsername());
            ps.setString(2, updatedHashedPassword);
            ps.setString(3, updatedUser.getRole());
            // execute update
            ps.executeUpdate();
            // retrieve updated user from DB
            User user = getUserByUserName(updatedUser.getUsername());
            // clear password before returning
            user.setPassword("");
            // return newly created user object
            return user;
        }
        catch (SQLException e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> getAll() {
        // list to store all users
        List<User> users = new ArrayList<>();
        // parameterized SQL to select all users
        String sql = """
                SELECT
                    *
                FROM
                    users""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // execute query
            ResultSet row = statement.executeQuery();
            // map each row to a User object
            while (row.next()) {
                User user = mapRow(row);
                // add user to list of users
                users.add(user);
            }
        } catch (SQLException e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
        return users;
    }

    @Override
    public User getUserById(int id) {
        // parameterized SQL to select user by ID
        String sql = """
                SELECT
                    *
                FROM
                    users
                WHERE
                    user_id = ?""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind ID parameter
            statement.setInt(1, id);
            // execute query
            ResultSet row = statement.executeQuery();
            // return mapped user if exists
            if (row.next()) {
                return mapRow(row);
            }
        } catch (SQLException e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public User getUserByUserName(String username) {
        // parameterized SQL to select user by username
        String sql = """
                SELECT
                    *
                FROM
                    users
                WHERE
                    username = ?""";

        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // bind username parameter
            statement.setString(1, username);
            // execute query
            ResultSet row = statement.executeQuery();
            if (row.next()) {
                return mapRow(row);
            }
        } catch (SQLException e) { // wrap and rethrow exception
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public int getIdByUsername(String username) {
        // retrieve user by username and return ID
        User user = getUserByUserName(username);

        if(user != null) {
            return user.getId();
        }
        // return -1 if user does not exist
        return -1;
    }

    @Override
    public boolean exists(String username) {
        // check if a user exists by username
        User user = getUserByUserName(username);
        return user != null;
    }

    private User mapRow(ResultSet row) throws SQLException {
        // map ResultSet row to a User object
        int userId = row.getInt("user_id");
        String username = row.getString("username");
        String hashedPassword = row.getString("hashed_password");
        String role = row.getString("role");
        // create user, set appropriate id, and return it
        User user = new User(username, hashedPassword, role);
        user.setId(userId);
        return user;
    }
}
