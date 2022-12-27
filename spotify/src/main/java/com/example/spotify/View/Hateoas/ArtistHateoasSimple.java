package com.example.spotify.View.Hateoas;

import com.example.spotify.Controller.ArtistController;
import com.example.spotify.Model.Artist.Artist;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ArtistHateoasSimple implements RepresentationModelAssembler<Artist, EntityModel<Artist>> {
    @Override
    public EntityModel<Artist> toModel(Artist artist) {

        return EntityModel.of(artist,
                WebMvcLinkBuilder.linkTo(methodOn(ArtistController.class).getArtistByUuid(artist.getUuid())).withSelfRel(), //link-ul catre resursa
                linkTo(methodOn(ArtistController.class).getAllArtists(Optional.empty(), Optional.empty())).withRel("parent") // link-ul catre metoda pe colectia de resure
        );
    }
}
