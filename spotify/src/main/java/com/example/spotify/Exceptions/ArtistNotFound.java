package com.example.spotify.Exceptions;

public class ArtistNotFound extends RuntimeException{
    public ArtistNotFound(String uuid) {
        super(String.format("Artist with uuid - %s does not exist!", uuid));
    }
}
