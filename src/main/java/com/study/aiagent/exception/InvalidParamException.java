package com.study.aiagent.exception;

import java.io.Serial;

/**
 * 参数无效异常
 */
public class InvalidParamException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -7034897190745766939L;

    public InvalidParamException() {
        super();
    }

    public InvalidParamException(String message) {
        super(message);
    }

    public InvalidParamException(String message, Throwable cause) {
        super(message, cause);
    }
}
