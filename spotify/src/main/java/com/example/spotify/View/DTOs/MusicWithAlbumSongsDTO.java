package com.example.spotify.View.DTOs;

import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Music.Music;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MusicWithAlbumSongsDTO {
    @Getter
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer year;

    @Getter
    @Setter
    private Music.TYPE type;

    @Getter
    @Setter
    private Music.GENRE genre;

    @Getter
    @Setter
    private EntityModel<SimpleMusicDTO> album;

    @Getter
    @Setter
    private List<EntityModel<SimpleMusicDTO>> albumSongs;

    @Getter
    @Setter
    private List<EntityModel<Artist>> artist;

    public MusicWithAlbumSongsDTO(Music music) {
        this.id = music.getId();
        this.name = music.getName();
        this.year = music.getYear();;
        this.type = music.getType();
        this.genre = music.getGenre();
        this.artist = new ArrayList<>();
    }

    public void addArtist(EntityModel<Artist> artist)
    {
        this.artist.add(artist);
    }
}
