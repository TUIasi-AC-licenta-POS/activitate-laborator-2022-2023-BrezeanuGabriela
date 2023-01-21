package com.example.spotify.Services.Artist;

import com.example.spotify.Controller.ArtistController;
import com.example.spotify.Exceptions.ArtistAlreadyExists;
import com.example.spotify.Exceptions.ArtistNotFound;
import com.example.spotify.Exceptions.ArtistUuidException;
import com.example.spotify.Exceptions.JPAException;
import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Artist.ArtistRepository;
import com.example.spotify.Model.Music.Music;
import com.example.spotify.Model.Music.MusicRepository;
import com.example.spotify.Model.MusicArtists.MusicArtist;
import com.example.spotify.Model.MusicArtists.MusicArtistIds;
import com.example.spotify.Model.MusicArtists.MusicArtistRepository;
import com.example.spotify.View.DTOs.ArtistSongsDTO;
import com.example.spotify.View.DTOs.SimpleMusicWithTypeDTO;
import com.example.spotify.View.Hateoas.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class ArtistService implements IArtist{
    @Autowired
    private MusicArtistRepository musicArtistRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private ArtistHateoasSimple artistHateoasSimple;

    @Autowired
    private ArtistHateoasDelete artistHateoasDelete;

    @Autowired
    private MusicTypeHateoasSimple musicHateoasVerySimple;

    public ArtistService(ArtistRepository artistRepository)
    {
        this.artistRepository = artistRepository;
    }

    @Override
    public CollectionModel<EntityModel<Artist>> getAllArtists()
    {
        List<EntityModel<Artist>> listArtists = artistRepository.findAll().stream().map(artistHateoasSimple::toModel).collect(Collectors.toList());
        Link selfLink = linkTo(methodOn(ArtistController.class).getAllArtists(Optional.empty(), Optional.empty())).withSelfRel();
        CollectionModel<EntityModel<Artist>> result = CollectionModel.of(listArtists, selfLink);

        return  result;
    }

    @Override
    public EntityModel<Artist> getArtistByUuid(String uuid)
    {
        Artist artist = artistRepository.findArtistByUuid(uuid);
        if(artist != null)
            return artistHateoasSimple.toModel(artist);
        else
            throw new ArtistNotFound(uuid);
    }

    @Override
    public EntityModel<ArtistSongsDTO> getArtistByName(String name, Optional<String> match)
    {
        List<Music> listMusic = new ArrayList<>();
        Artist artist = null;
        Link selfLink = linkTo(
                methodOn(ArtistController.class)
                .getAllArtists(Optional.empty(), Optional.empty()))
                .withSelfRel();

        if(!match.isPresent() || (match.isPresent() && match.get().compareTo("exact") == 0))
        {
            artist = artistRepository.findArtistByName(name);
            if(artist != null) {
                listMusic = musicRepository.findMusicByArtists(artist);
                if(match.isPresent())
                {
                    selfLink = linkTo(methodOn(ArtistController.class).getAllArtists(Optional.of(name), Optional.of(match.get()))).withSelfRel();
                }
                selfLink = linkTo(methodOn(ArtistController.class).getAllArtists(Optional.of(name), Optional.empty())).withSelfRel();
            }
        }
        if(match.isPresent() && match.get().compareTo("partial") == 0) {
            artist = artistRepository.findArtistByNameLike("%" + name + "%");
            if(artist != null) {
                listMusic = musicRepository.findMusicByArtists(artist);
                selfLink = linkTo(methodOn(ArtistController.class).getAllArtists(Optional.of(name), Optional.of(match.get()))).withSelfRel();
            }
        }

        if(artist != null) {
            ArtistSongsDTO artistSongsDTO = new ArtistSongsDTO(artist.getUuid(), artist.getName());
            List<EntityModel<SimpleMusicWithTypeDTO>> listResult = listMusic
                    .stream()
                    .map(m -> new SimpleMusicWithTypeDTO(m.getId(), m.getName(), m.getGenre(), m.getType()))
                    .map(musicHateoasVerySimple::toModel)
                    .collect(Collectors.toList());

            artistSongsDTO.setSongsAndAlbums(listResult);

            EntityModel<ArtistSongsDTO> result = EntityModel.of(artistSongsDTO, selfLink);
            return result;
        }
        else {
            throw new RuntimeException("artist not found");
        }
    }

    @Override
    public EntityModel<Artist> addArtist(Artist artist)
    {
        // constrangere pentru ca numele sa nu fie empty
        if(artist.getName().equals(""))
            throw new RuntimeException("Name is empty...");

        if(artistRepository.findArtistByName(artist.getName()) != null)
            throw new JPAException("Name already exists!");

        // create
        if(artistRepository.findArtistByUuid(artist.getUuid()) == null )
        {
            if(artist.getUuid().length() != 36)
            {
                throw new RuntimeException("UUid is incorect. Please check it!");
            }
            try {
                artistRepository.save(artist);
            }
            catch (DataAccessException dataAccessException) {
                throw new JPAException(dataAccessException.getRootCause().toString());
            }
        }

        artist = artistRepository.findArtistByUuid(artist.getUuid());
        return artistHateoasSimple.toModel(artist);
    }

    @Override
    public EntityModel<Artist> updateArtist(Artist artist, String oldUuid)
    {
        if(artistRepository.findArtistByUuid(oldUuid) != null )
        {
            if(artist.getUuid().length() != 36)
            {
                throw new RuntimeException("UUid is incorrect. Please check it!");
            }

            // trebuie sa inlocuiesc intrarile din music_artists
            List<Integer> listArtistSongsIds = musicArtistRepository.findMusicArtistByIdArtist(artist.getUuid())
                    .stream().map(MusicArtist::getIdMusic).collect(Collectors.toList());

            // actualizez datele despre artist
            try {
                artistRepository.deleteArtistByUuid(oldUuid);
                artistRepository.save(artist);
            }catch (DataAccessException dataAccessException) {
                throw new JPAException(dataAccessException.getRootCause().toString());
            }

            for(Integer idMusic:listArtistSongsIds)
            {
                musicArtistRepository.save(new MusicArtist(new MusicArtistIds(idMusic, artist.getUuid())));
            }
        }

        artist = artistRepository.findArtistByUuid(artist.getUuid());

        return artistHateoasSimple.toModel(artist);
    }

    @Override
    public EntityModel<Artist> deleteArtist(String uuid)
    {
        Artist artist = artistRepository.findArtistByUuid(uuid);
        if(artist != null) {
            EntityModel deletedArtist = artistHateoasDelete.toModel(artist);
            try {
                artistRepository.deleteArtistByUuid(artist.getUuid());
                return deletedArtist;
            }catch (DataAccessException dataAccessException) {
                throw new JPAException(dataAccessException.getRootCause().toString());
            }
        }
        else
        {
            throw new RuntimeException("Artist uuid not found!");
        }
    }
}
