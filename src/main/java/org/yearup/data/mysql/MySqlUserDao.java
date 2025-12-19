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

/**
 * MySQL implementation of the UserDao interface.
 * This class provides database operations for managing user accounts,
 * including creation, updates, and retrieval of user records.
 * It uses JDBC with parameterized SQL queries to ensure secure
 * interaction with the database. Passwords are securely hashed
 * using BCrypt before being stored.
 */
@Component
public class MySqlUserDao extends MySqlDaoBase implements UserDao {
    // constructor to initialize the data source
    @Autowired
    public MySqlUserDao(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Creates a new user record in the database.
     * This method inserts a new user with a username, hashed password,
     * and role into the users table. The password is encrypted using
     * BCrypt before being stored for security purposes. After insertion,
     * the generated user ID is retrieved and the complete user object
     * is returned with the password cleared.
     */
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

    /**
     * Updates an existing user's information in the database.
     * This method updates the username, hashed password, and role
     * for a user based on the provided user ID. The password is
     * re-hashed using BCrypt before being saved. After the update,
     * the refreshed user record is retrieved and returned with the
     * password field cleared.
     */
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

    /**
     * Retrieves all users from the database.
     * This method executes a query to select every user record
     * from the users table. Each result row is mapped to a User
     * object and added to a list. The complete list of users
     * is returned to the caller.
     */
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

    /**
     * Retrieves a user from the database using the user's unique ID.
     * This method executes a parameterized query to safely locate
     * the user record. If a matching user is found, it is mapped
     * to a User object and returned. If no user exists with the
     * given ID, the method returns null.
     */
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

    /**
     * Retrieves a user from the database using a username.
     * This method executes a parameterized query to safely search
     * for the user record by username. If a matching user is found,
     * it is mapped to a User object and returned. If no match exists,
     * the method returns null.
     */
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

    /**
     * Retrieves the user ID associated with a given username.
     * This method first retrieves the full user record and then
     * extracts the user ID from the result. If the user exists,
     * the corresponding ID is returned. If the username is not
     * found, the method returns -1.
     */
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

    /**
     * Checks whether a user exists in the database.
     * This method determines existence by attempting to retrieve
     * a user record using the provided username. If a user record
     * is found, the method returns true. If no user exists with
     * that username, the method returns false.
     */
    @Override
    public boolean exists(String username) {
        // check if a user exists by username
        User user = getUserByUserName(username);
        return user != null;
    }

    /**
     * Maps a single database result set row to a User object.
     * This method extracts the user ID, username, hashed password,
     * and role from the current row. A new User object is created
     * and populated with these values. This method is used internally
     * to convert database records into User objects.
     */
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
