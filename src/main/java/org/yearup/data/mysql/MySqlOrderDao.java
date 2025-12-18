package org.yearup.data.mysql;

import org.yearup.data.OrderDao;
import org.yearup.models.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {

    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Order create(Order order) {

        String sql = """
                INSERT INTO
                    orders (user_id, date, address, city, state, zip, shipping_amount)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection()) {

            PreparedStatement preparedStatement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setTimestamp(2, java.sql.Timestamp.valueOf(order.getDate()));
            preparedStatement.setString(3, order.getAddress());
            preparedStatement.setString(4, order.getCity());
            preparedStatement.setString(5, order.getState());
            preparedStatement.setString(6, order.getZip());
            preparedStatement.setBigDecimal(7, order.getShippingAmount());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected.");

            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    order.setOrderId(resultSet.getInt(1));
                }
            }
            return order;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
