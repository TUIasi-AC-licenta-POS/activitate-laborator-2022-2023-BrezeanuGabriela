package com.example.spotify.View.DTOs;

import com.example.spotify.Model.Music.Music;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.EntityModel;

import java.util.List;

public class SimpleMusicDTO {
    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Music.GENRE genre;

    public SimpleMusicDTO(Integer id, String name, Music.GENRE genre)
    {
        this.id = id;
        this.name = name;
        this.genre = genre;
    }

}
