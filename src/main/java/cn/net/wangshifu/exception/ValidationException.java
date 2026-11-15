package cn.net.wangshifu.exception;

/** Thrown when user input fails validation; mapped to a 400 with the message shown to the user. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}