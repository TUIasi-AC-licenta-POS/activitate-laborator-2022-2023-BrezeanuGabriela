package com.example.spotify.View.DTOs;

import com.example.spotify.Model.Music.Music;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class NewMusicDTO {
    @Getter
    @Setter
    @Id
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Integer year;

    @Getter
    private Music.TYPE type;

    @Getter
    private Music.GENRE genre;

    @Setter
    @Getter
    private Integer idAlbum;

    @Setter
    @Getter
    @Transient
    private List<String> idsArtist = new ArrayList<>();

    public NewMusicDTO() {}


    public void setGenre(String genre){
        switch (genre)
        {
            case "rock" : {
                this.genre = Music.GENRE.rock;
                break;
            }
            case "metal" : {
                this.genre = Music.GENRE.metal;
                break;
            }
            case "pop" : {
                this.genre = Music.GENRE.pop;
                break;
            }
            default: {
                this.genre = Music.GENRE.unknown;
                break;
            }
        }
    }

    public void setType(String type)
    {
        switch (type) {
            case "song" : {
                this.type = Music.TYPE.song;
                break;
            }
            case "single" : {
                this.type = Music.TYPE.single;
                break;
            }
            case "album" : {
                this.type = Music.TYPE.album;
                break;
            }
            default: {
                this.type = Music.TYPE.unknown;
                break;
            }
        }
    }
}
