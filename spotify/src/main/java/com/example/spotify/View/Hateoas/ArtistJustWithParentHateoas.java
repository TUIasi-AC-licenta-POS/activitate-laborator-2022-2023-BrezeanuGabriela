package com.example.spotify.View.Hateoas;

import com.example.spotify.Controller.ArtistController;
import com.example.spotify.Controller.MusicController;
import com.example.spotify.Model.Artist.Artist;
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
public class ArtistJustWithParentHateoas implements RepresentationModelAssembler<Object, EntityModel<Object>> {
@Override
public EntityModel<Object> toModel(Object artist)
        {
        return EntityModel.of(artist,
                linkTo(methodOn(ArtistController.class).getAllArtists(Optional.empty(),Optional.empty())).withRel("parent")
        );
        }
}