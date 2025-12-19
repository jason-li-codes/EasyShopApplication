package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao {

    public MySqlProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {

        String sql = """
                INSERT INTO
                    profiles (user_id, first_name, last_name, phone, email, address, city, state, zip)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?);""";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile update(int userId, Profile profile) {

        String sql = """
                INSERT INTO
                    profiles (user_id, first_name, last_name, phone, email, address, city, state, zip)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?, ?, ?)""";

        try (Connection connection = getConnection()) {

            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getProfileByUserId(int id) {

        String sql = """
                SELECT
                    *
                FROM
                    profiles
                WHERE
                    user_id = ?""";

        Profile profile = new Profile();
        try (Connection connection = getConnection()) {

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

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

                return profile;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
