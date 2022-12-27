package com.example.spotify.Exceptions;

public class JPAException extends RuntimeException{
    public JPAException(String message)
    {
        super(message);
    }
}
