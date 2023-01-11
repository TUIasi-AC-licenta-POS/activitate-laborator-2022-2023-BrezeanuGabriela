package com.example.spotify.View.Hateoas;

import com.example.spotify.Controller.MusicController;
import com.example.spotify.Model.Music.Music;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MusicHateoasDelete implements RepresentationModelAssembler<Music, EntityModel<Music>>{
    @Override
    public EntityModel<Music> toModel(Music music)
    {
        return EntityModel.of(music,
                linkTo(methodOn(MusicController.class).
                        getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                        .withRel("parent") // link-ul catre metoda pe colectia de resure
        );
    }
}