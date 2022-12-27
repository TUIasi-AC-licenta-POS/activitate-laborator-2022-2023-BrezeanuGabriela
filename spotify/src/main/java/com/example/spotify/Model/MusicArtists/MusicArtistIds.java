package com.example.spotify.Model.MusicArtists;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

public class MusicArtistIds implements Serializable {
    @Setter
    @Getter
    @Column(name="id_music")
    private Integer idMusic;


    @Setter
    @Getter
    @Column(name="id_artist")
    private String idArtist;

    public MusicArtistIds() {}

    public MusicArtistIds(Integer id_music, String id_artist) {
        this.idMusic = id_music;
        this.idArtist = id_artist;
    }
}
