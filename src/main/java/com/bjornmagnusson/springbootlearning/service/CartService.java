package com.bjornmagnusson.springbootlearning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bjornmagnusson.springbootlearning.model.Cart;
import com.bjornmagnusson.springbootlearning.model.CartItem;
import com.bjornmagnusson.springbootlearning.model.Product;
import com.bjornmagnusson.springbootlearning.repository.CartItemRepository;
import com.bjornmagnusson.springbootlearning.repository.CartRepository;

@Service
public class CartService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);
    private final CartRepository cartRepository;
    private CartItemRepository cartItemRepository;

    public CartService(CartRepository repository, CartItemRepository cartItemRepository) {
        this.cartRepository = repository;
        this.cartItemRepository = cartItemRepository;
    }

    public void addProduct(Product product) {
        var cart = getCart();
        var products = cart.getProducts();
        var cartItemOptional = products.stream().filter(cartItem -> cartItem.getId() == product.getId()).findFirst();
        var number = 1;
        if (cartItemOptional.isPresent()) {
            number = cartItemOptional.get().getNumber() + 1;
            cartItemOptional.get().setNumber(number);
        } else {
            var cartItem = new CartItem(product.getId(), number);
            cartItem.setCart(cart);
            var persistedCartItem = cartItemRepository.save(cartItem);
            products.add(persistedCartItem);
        }
        LOGGER.info(products.toString());
        LOGGER.info("Product ({}) increasing to {}", product, products.stream().filter(cartItem -> cartItem.getId() == product.getId()).findFirst().get().getNumber());
        var persistedCart = cartRepository.save(cart);
        LOGGER.info(persistedCart.getProducts().toString());
        LOGGER.info("Product ({}) increased to {}", product, persistedCart.getProducts().stream().filter(cartItem -> cartItem.getId() == product.getId()).findFirst().get().getNumber());
    }

    public Cart getCart() {
        var iterator = cartRepository.findAll().iterator();
        if (iterator.hasNext()) {
            LOGGER.info("Cart exist");
            return iterator.next();
        }
        LOGGER.info("Cart new");
        return cartRepository.save(new Cart());
    }

    public void removeProduct(int id) {
        var cart = getCart();
        var products = cart.getProducts();
        var cartItemOptional = products.stream().filter(cartItem -> cartItem.getId() == id).findFirst();
        var number = 0;
        if (cartItemOptional.isPresent()) {
            var cartItem = cartItemOptional.get();
            number = cartItem.getNumber() - 1;
            cartItem.setNumber(number);
            if (number == 0) {
                products.remove(cartItem);
                cartRepository.save(cart);
                cartItemRepository.delete(cartItem);
                LOGGER.info("Product ({}) removed", id);
            } else {
                cartItem.setNumber(number);
                cartItemRepository.save(cartItem);
                cartRepository.save(cart);
                LOGGER.info("Product ({}) decreased to {}", id, number);                
            }            
        }        
    }
}
