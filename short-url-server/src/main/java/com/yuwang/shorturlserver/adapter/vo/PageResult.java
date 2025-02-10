package com.yuwang.shorturlserver.adapter.vo;

import lombok.Getter;

@Getter
public class PageResult<T> extends BaseResult<T> {
    private final long total;
    private final long pageSize;
    private final long current;
    private final long pages;

    private PageResult(int code, String message, T data, String requestId,
                       long total, long pageSize, long current, long pages) {
        super(code, message, data, requestId);
        this.total = total;
        this.pageSize = pageSize;
        this.current = current;
        this.pages = pages;
    }

    public static <T> PageResult<T> success(T data, long total, long pageSize, long current) {
        long pages = (total / pageSize) + 1;
        return new PageResult<>(SUCCESS, SUCCESS_STR, data, null, total, pageSize, current, pages);
    }

    public static <T> PageResult<T> error(String message) {
        return new PageResult<>(FAILED, message, null, null, 0, 0, 0, 0);
    }

    public static <T> PageResult<T> error(int code, String message) {
        return new PageResult<>(code, message, null, null, 0, 0, 0, 0);
    }
}