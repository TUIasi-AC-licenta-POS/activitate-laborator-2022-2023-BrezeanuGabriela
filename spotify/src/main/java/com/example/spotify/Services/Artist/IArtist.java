package com.example.spotify.Services.Artist;

import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Music.Music;
import com.example.spotify.View.DTOs.ArtistSongsDTO;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.util.Optional;

public interface IArtist {
    EntityModel<Artist> addArtist(Artist artist);

    EntityModel<Artist> updateArtist(Artist artist, String oldUuid);

    CollectionModel<EntityModel<Artist>> getAllArtists();

    EntityModel<Artist> getArtistByUuid(String uuid);

    EntityModel<Artist> deleteArtist(String uuid);

    EntityModel<ArtistSongsDTO> getArtistByName(String name, Optional<String> match);
}
