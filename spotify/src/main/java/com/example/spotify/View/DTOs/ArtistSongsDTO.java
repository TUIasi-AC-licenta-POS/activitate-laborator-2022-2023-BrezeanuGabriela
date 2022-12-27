package com.example.spotify.View.DTOs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.EntityModel;

import java.util.List;

public class ArtistSongsDTO {
    @Getter
    private String uuid;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private List<EntityModel<SimpleMusicWithTypeDTO>> songsAndAlbums;

    public ArtistSongsDTO(String uuid, String name)
    {
        this.uuid = uuid;
        this.name = name;
    }
}
