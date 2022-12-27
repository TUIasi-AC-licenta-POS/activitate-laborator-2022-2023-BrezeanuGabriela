package com.example.spotify.View.DTOs;

import com.example.spotify.Model.Music.Music;
import lombok.Getter;
import lombok.Setter;

public class SimpleMusicWithTypeDTO {
    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Music.GENRE genre;

    @Getter
    @Setter
    private Music.TYPE type;

    public SimpleMusicWithTypeDTO(Integer id, String name, Music.GENRE genre, Music.TYPE type)
    {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.type = type;
    }
}
