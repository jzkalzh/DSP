package org.example.mockdb.controller;

import org.example.mockdb.model.ApiResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<String> illegalArgument(IllegalArgumentException e) {
        return ApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ApiResponse<String> dataAccess(DataAccessException e) {
        return ApiResponse.fail("数据库操作失败: " + e.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> exception(Exception e) {
        return ApiResponse.fail("程序异常: " + e.getMessage());
    }
}
