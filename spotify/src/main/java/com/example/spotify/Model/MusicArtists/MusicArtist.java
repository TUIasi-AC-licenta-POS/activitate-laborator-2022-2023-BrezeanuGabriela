package com.example.spotify.Model.MusicArtists;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;

@Entity
@Table(name="music_artists")
@IdClass(MusicArtistIds.class)
public class MusicArtist{

    @Id
    @Setter
    @Getter
    @Column(name="id_music")
    private Integer idMusic;

    @Id
    @Setter
    @Getter
    @Column(name="id_artist")
    private String idArtist;

    public MusicArtist() {}

    public MusicArtist(MusicArtistIds musicArtistIds) {
        this.idMusic = musicArtistIds.getIdMusic();
        this.idArtist = musicArtistIds.getIdArtist();
    }
}
