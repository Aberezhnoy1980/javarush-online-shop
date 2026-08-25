package ru.berezhnoy.shop.web;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
