package com.example.spotify.View.Hateoas;

import com.example.spotify.Controller.MusicController;
import com.example.spotify.View.DTOs.MusicCompleteDTO;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MusicHateoasComplete implements RepresentationModelAssembler<MusicCompleteDTO, EntityModel<MusicCompleteDTO>> {

    @Override
    public EntityModel<MusicCompleteDTO> toModel(MusicCompleteDTO music) {
        Link link = Link.of("http://127.0.0.1:8081/api/playlists/{idPlaylist}?operation=add");
        return EntityModel.of(music,
                WebMvcLinkBuilder.linkTo(methodOn(MusicController.class)
                        .getMusicById(music.getId())).withSelfRel(), //link-ul catre resursa
                linkTo(methodOn(MusicController.class).
                        getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                        .withRel("parent"), // link-ul catre metoda pe colectia de resurse
                link.withRel("add-to-playlist").withType("PATCH")
        );
    }
}
