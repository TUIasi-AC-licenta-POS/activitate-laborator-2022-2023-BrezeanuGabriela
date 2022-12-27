package com.example.spotify.Exceptions;

public class ArtistAlreadyExists extends RuntimeException{
    public ArtistAlreadyExists() {
        super("Uuid exists! Please try with different one!");
    }
}
