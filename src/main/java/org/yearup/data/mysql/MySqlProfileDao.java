package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao {
    // constructor to initialize the data source
    public MySqlProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                INSERT INTO
                    profiles (user_id, first_name, last_name, phone, email, address, city, state, zip)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?);""";

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // bind parameters for prepared statement
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());
            // execute update
            ps.executeUpdate();
            // return Profile object
            return profile;
        } catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile update(int userId, Profile profile) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                UPDATE
                    profiles
                SET
                    first_name = ?,
                    last_name = ?,
                    phone = ?,
                    email = ?,
                    address = ?,
                    city = ?,
                    state = ?,
                    zip = ?
                WHERE
                    user_id = ?""";

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            ps.setString(1, profile.getFirstName());
            ps.setString(2, profile.getLastName());
            ps.setString(3, profile.getPhone());
            ps.setString(4, profile.getEmail());
            ps.setString(5, profile.getAddress());
            ps.setString(6, profile.getCity());
            ps.setString(7, profile.getState());
            ps.setString(8, profile.getZip());
            ps.setInt(9, userId);
            // execute update
            ps.executeUpdate();
            // return Profile object
            return profile;
        } catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getProfileByUserId(int id) {
        // parameterized SQL to prevent SQL injection
        String sql = """
                SELECT
                    *
                FROM
                    profiles
                WHERE
                    user_id = ?""";
        // instantiate new empty Profile
        Profile profile = new Profile();

        try (Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // bind parameters for prepared statement
            preparedStatement.setInt(1, id);
            // execute query
            ResultSet resultSet = preparedStatement.executeQuery();
            // if query is successful, set profile attributes to correct values
            if (resultSet.next()) {
                profile.setUserId(id);
                profile.setFirstName(resultSet.getString("first_name"));
                profile.setLastName(resultSet.getString("last_name"));
                profile.setPhone(resultSet.getString("phone"));
                profile.setEmail(resultSet.getString("email"));
                profile.setAddress(resultSet.getString("address"));
                profile.setCity(resultSet.getString("city"));
                profile.setState(resultSet.getString("state"));
                profile.setZip(resultSet.getString("zip"));
                // return Profile object
                return profile;
            }
        } catch (SQLException e) { // wrap exception
            throw new RuntimeException(e);
        }
        return null;
    }

}
