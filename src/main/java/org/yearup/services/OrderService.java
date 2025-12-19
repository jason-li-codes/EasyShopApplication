package org.yearup.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineItemDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OrderService {
    // class contains the following attributes
    private OrderDao orderDao;
    private OrderLineItemDao orderLineItemDao;
    private ShoppingCartDao shoppingCartDao;
    private ProfileDao profileDao;

    @Autowired
    // constructor to inject required DAOs
    public OrderService(OrderDao orderDao, OrderLineItemDao orderLineItemDao,
                        ShoppingCartDao shoppingCartDao, ProfileDao profileDao) {
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.shoppingCartDao = shoppingCartDao;
        this.profileDao = profileDao;
    }

    public Order checkout(int userId) {

        Order order = new Order();

        createOrder(userId, order);

        ShoppingCart cart = shoppingCartDao.getByUserId(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty.");
        }

        order.setShippingAmount(cart.getTotal());
        orderDao.create(order);

        Map<Integer, ShoppingCartItem> itemList = cart.getItems();

        for (Map.Entry<Integer, ShoppingCartItem> i : itemList.entrySet()) {

            OrderLineItem orderLineItem = new OrderLineItem();

            int productId = i.getKey();
            Product product = i.getValue().getProduct();

            orderLineItem.setOrderId(order.getOrderId());
            orderLineItem.setProductId(productId);
            orderLineItem.setSalesPrice(product.getPrice());
            orderLineItem.setQuantity(i.getValue().getQuantity());
            orderLineItem.setDiscount(i.getValue().getDiscountPercent());

            orderLineItemDao.create(orderLineItem);
        }

        shoppingCartDao.deleteShoppingCart(userId);
        return order;
    }

    @Transactional
    private void createOrder(int userId, Order order) {

        Profile profile = profileDao.getProfileByUserId(userId);

        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile not found.");
        }

        LocalDateTime dateTime = LocalDateTime.now();

        String address = profile.getAddress();
        String state = profile.getState();
        String city = profile.getCity();
        String zip = profile.getZip();

        order.setUserId(userId);
        order.setDate(dateTime);
        order.setAddress(address);
        order.setState(state);
        order.setCity(city);
        order.setZip(zip);
        order.setShippingAmount(new BigDecimal("0"));
    }
}