package com.atguigu.core.exception;

public class MemberException extends RuntimeException {

    public MemberException(String message) {
        super(message);
    }

    public MemberException() {
        super();
    }
}
