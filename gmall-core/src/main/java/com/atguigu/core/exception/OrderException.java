package com.atguigu.core.exception;

public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }

    public OrderException() {
        super();
    }
}
