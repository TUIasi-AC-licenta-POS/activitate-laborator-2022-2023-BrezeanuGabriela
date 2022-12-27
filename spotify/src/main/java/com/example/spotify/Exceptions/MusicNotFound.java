package com.example.spotify.Exceptions;

public class MusicNotFound extends RuntimeException{
    public MusicNotFound() {
        super("Music does not exist");
    }
    public MusicNotFound(String message)
    {
        super(message);
    }
}
