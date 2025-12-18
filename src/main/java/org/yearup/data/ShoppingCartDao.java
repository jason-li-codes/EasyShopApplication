package org.yearup.data;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.math.BigDecimal;

public interface ShoppingCartDao {


    ShoppingCart getByUserId(int userId);
    ShoppingCart getByOrderId(int orderId);
    ShoppingCart addItemShoppingCart(int userId, ShoppingCartItem item);
    ShoppingCart updateItemShoppingCart(int userId, ShoppingCartItem item);
    void deleteShoppingCart(int userId);
    BigDecimal getShoppingCartTotal(ShoppingCart shoppingCart);

}
