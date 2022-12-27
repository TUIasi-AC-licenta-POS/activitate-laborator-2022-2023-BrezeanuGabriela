package com.example.spotify.Exceptions;

public class MusicIdDoesNotExist extends RuntimeException{
    public MusicIdDoesNotExist(Integer id)
    {
        super(String.format("Music with id - %d does not exist!", id));
    }
}
