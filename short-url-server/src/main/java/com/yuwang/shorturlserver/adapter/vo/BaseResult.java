package com.yuwang.shorturlserver.adapter.vo;

import lombok.Builder;

@Builder
public class BaseResult<T> {
    public static final int SUCCESS = 0;
    public static final int FAILED = 1;
    public static final String SUCCESS_STR = "success";
    private int code;
    private String message;
    private T data;
    private String requestId;

    public static <T> BaseResult<T> success(T data) {
        return BaseResult.<T>builder().code(SUCCESS).message(SUCCESS_STR).data(data).build();
    }

    public static <T> BaseResult<T> error(String message) {
        return BaseResult.<T>builder().code(FAILED).message(message).build();
    }

    public static <T> BaseResult<T> error(int code, String message) {
        return BaseResult.<T>builder().code(FAILED).message(message).build();
    }
}
