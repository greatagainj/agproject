package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public Resp<Object> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);
        return Resp.ok(null);
    }

    @GetMapping
    public Resp<List<Cart>> queryCarts() {
        List<Cart> carts= this.cartService.queryCarts();
        return Resp.ok(carts);
    }

    @GetMapping("{userId}")
    public Resp<List<Cart>> queryCartsByUserId(@PathVariable("userId") Long uerId) {
        List<Cart> carts= this.cartService.queryCartsByUserId(uerId);
        return Resp.ok(carts);
    }

    @PostMapping("update")
    public Resp<Object> updateCart(@RequestBody Cart cart) {
        this.cartService.updateCart(cart);
        return Resp.ok(null);
    }

    @PostMapping("delete/{skuId}")
    public Resp<Object> deleteCart(@PathVariable("skuId")Long skuId) {
        this.cartService.deleteCart(skuId);
        return Resp.ok(null);
    }
}
