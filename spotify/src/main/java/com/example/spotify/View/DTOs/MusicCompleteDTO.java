package com.example.spotify.View.DTOs;

import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Music.Music;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

public class MusicCompleteDTO {
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
    private Music album;

    @Getter
    @Setter
    private Set<Music> albumSongs;

    @Getter
    @Setter
    private Set<Artist> artists = new HashSet<>();

    public MusicCompleteDTO(Music music)
    {
        this.id = music.getId();
        this.name = music.getName();
        this.year = music.getYear();;
        this.type = music.getType();
        this.genre = music.getGenre();
        this.album = music.getAlbum();
        this.albumSongs = music.getAlbumSongs();
        //this.artists = music.getArtists();
        this.artists = new HashSet<>();
    }

    public void addArtist(Artist artist)
    {
        artists.add(artist);
    }
}
