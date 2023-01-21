package com.example.spotify.Services.Music;

import com.example.spotify.Controller.ArtistController;
import com.example.spotify.Controller.MusicController;
import com.example.spotify.Exceptions.*;
import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Artist.ArtistRepository;
import com.example.spotify.Model.Music.Music;
import com.example.spotify.Model.Music.MusicRepository;
//import com.example.spotify.View.MusicFromArtistDTO;
import com.example.spotify.Model.MusicArtists.MusicArtist;
import com.example.spotify.Model.MusicArtists.MusicArtistIds;
import com.example.spotify.Model.MusicArtists.MusicArtistRepository;
import com.example.spotify.View.DTOs.MusicCompleteDTO;
import com.example.spotify.View.Hateoas.MusicHateoasSimple;
import com.example.spotify.View.DTOs.NewMusicDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class MusicService implements IMusic {
    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private MusicArtistRepository musicArtistRepository;

    @Autowired
    private MusicHateoasSimple musicHateoasSimple;

    private Integer items_per_page = 3;

    public MusicService(MusicRepository musicRepository, ArtistRepository artistRepository, MusicArtistRepository musicArtistRepository)
    {
        this.musicRepository = musicRepository;
        this.artistRepository = artistRepository;
        this.musicArtistRepository = musicArtistRepository;
    }

    @Override
    public CollectionModel<EntityModel<Music>> getAllMusic()
    {
        List<EntityModel<Music>> listMusic = musicRepository.findAll().stream().map(musicHateoasSimple::toModel).collect(Collectors.toList());

        if(listMusic.size() != 0) {
            Link selfLink = linkTo(methodOn(MusicController.class).
                    getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                    .withSelfRel();
            CollectionModel<EntityModel<Music>> result = CollectionModel.of(listMusic, selfLink);

            return result;
        }
        else
            throw new MusicNotFound();
    }

    @Override
    public CollectionModel<EntityModel<Music>> getMusicByNoPage(Integer page, Optional<Integer> items_per_page)
    {
        if(items_per_page.isPresent())
        {
            this.items_per_page = items_per_page.get();
        }

        List<Music> listMusic = musicRepository.findAll();
        Integer index_start = page * this.items_per_page ;
        Integer index_stop = index_start + this.items_per_page ;

        List<EntityModel<Music>> listResult = new ArrayList<>();
        for(Integer index = index_start; index < index_stop; index++)
        {
            if(index < listMusic.size())
                listResult.add(musicHateoasSimple.toModel(listMusic.get(index)));
        }
        this.items_per_page = 3;

        Link selfLink = linkTo(methodOn(MusicController.class).
                getAllMusic(Optional.of(page), items_per_page, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .withSelfRel();

        Optional<Integer> nextPage = Optional.of(page + 1);
        Optional<Integer> prevPage = Optional.of(page - 1);

        Link nextLink = linkTo(methodOn(MusicController.class).
                getAllMusic(nextPage, items_per_page, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .withRel("next");

        Link prevLink = linkTo(methodOn(MusicController.class).
                getAllMusic(prevPage, items_per_page, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .withRel("prev");

        Link parentLink = linkTo(methodOn(MusicController.class).
            getAllMusic(Optional.empty(), items_per_page, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
            .withRel("parent");

        // nu sunt elemente pe pagina solicitata
        if (listResult.size() == 0 && page != 0)
        {
            // determinam ultima pagina
            if(!items_per_page.isPresent()) {
                prevPage = Optional.of(listMusic.size() / this.items_per_page - 1);
                prevLink = linkTo(methodOn(MusicController.class).
                        getAllMusic(prevPage, items_per_page, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                        .withRel("prev");
                return CollectionModel.of(listResult, parentLink, prevLink);
            }
            else
            {
                prevPage = Optional.of(listMusic.size() / items_per_page.get() - 1);
                prevLink = linkTo(methodOn(MusicController.class).
                        getAllMusic(prevPage, items_per_page, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                        .withRel("prev");
                return CollectionModel.of(listResult, parentLink, prevLink);
            }
        }

        if (page == 0)
        {
            CollectionModel<EntityModel<Music>> result = CollectionModel.of(listResult, selfLink, nextLink);
            return result;
        }

        if (page == (listMusic.size() / this.items_per_page - 1))
        {
            CollectionModel<EntityModel<Music>> result = CollectionModel.of(listResult, selfLink, prevLink);
            return result;
        }

        CollectionModel<EntityModel<Music>> result = CollectionModel.of(listResult, selfLink, nextLink, prevLink);
        return result;
    }

    @Override
    public CollectionModel<EntityModel<Music>> getMusicByName(String name, Optional<String> match)
    {
        List<Music> listMusic = new ArrayList<>();
        Link selfLink = linkTo(methodOn(MusicController.class).
                getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .withSelfRel();

        if( !match.isPresent() || (match.isPresent() && match.get().compareTo("exact") == 0)) {
            listMusic = musicRepository.findMusicByName(name);

            if(match.isPresent()) {
                selfLink = linkTo(methodOn(MusicController.class).
                        getAllMusic(Optional.empty(), Optional.empty(), Optional.of(name), Optional.of(match.get()), Optional.empty(), Optional.empty()))
                        .withSelfRel();
            }
            selfLink = linkTo(methodOn(MusicController.class).
                    getAllMusic(Optional.empty(), Optional.empty(), Optional.of(name), Optional.empty(), Optional.empty(), Optional.empty()))
                    .withSelfRel();
        }

        if(match.isPresent() && match.get().compareTo("partial") == 0) {
            listMusic = musicRepository.findByNameLike("%" + name + "%");
            selfLink = linkTo(methodOn(MusicController.class).
                    getAllMusic(Optional.empty(), Optional.empty(), Optional.of(name), Optional.of(match.get()), Optional.empty(), Optional.empty()))
                    .withSelfRel();
        }

        List<EntityModel<Music>> listResult = listMusic.stream()
                .map(musicHateoasSimple::toModel)
                .collect(Collectors.toList());


        if(listResult.size() == 0)
        {
            Link parentLink = linkTo(methodOn(MusicController.class).
                    getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                    .withRel("parent");
            return CollectionModel.of(listResult, parentLink);
        }

        CollectionModel<EntityModel<Music>> result = CollectionModel.of(listResult, selfLink);
        return result;
    }

    @Override
    public CollectionModel<EntityModel<Music>> getMusicByYear(Integer year)
    {
        List<Music> listMusic = musicRepository.findMusicByYear(year);
        Link selfLink = linkTo(methodOn(MusicController.class).
                getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(year)))
                .withSelfRel();

        List<EntityModel<Music>> listResult = listMusic.stream()
                .map(musicHateoasSimple::toModel)
                .collect(Collectors.toList());


        if(listResult.size() == 0)
        {
            Link parentLink = linkTo(methodOn(MusicController.class).
                    getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                    .withRel("parent");
            return CollectionModel.of(listResult, parentLink);
        }

        CollectionModel<EntityModel<Music>> result = CollectionModel.of(listResult, selfLink);
        return result;
    }

    @Override
    public CollectionModel<EntityModel<Music>> getMusicByGenre(String genre) {
        try {
            Music.GENRE genreEnum = Music.GENRE.valueOf(genre);
            List<Music> listMusic = musicRepository.findMusicByGenre(genreEnum);

            Link selfLink = linkTo(methodOn(MusicController.class).
                    getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(genre), Optional.empty()))
                    .withSelfRel();

            List<EntityModel<Music>> listResult = listMusic.stream()
                    .map(musicHateoasSimple::toModel)
                    .collect(Collectors.toList());

            if(listResult.size() == 0)
            {
                Link parentLink = linkTo(methodOn(MusicController.class).
                        getAllMusic(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                        .withRel("parent");
                return CollectionModel.of(listResult, parentLink);
            }

            CollectionModel<EntityModel<Music>> result = CollectionModel.of(listResult, selfLink);
            return result;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            throw new RuntimeException(illegalArgumentException.getMessage());
        }
        catch (DataAccessException dataAccessException) {
            throw  new RuntimeException(dataAccessException.getRootCause().toString());
        }
        catch (RuntimeException runtimeException) {
            throw new RuntimeException(runtimeException.getMessage());
        }
    }

    @Override
    public Music getMusicById(Integer musicId)
    {
        Music music = musicRepository.findMusicById(musicId);
        
        if(music != null)
        {
            if(music.getIdAlbum() != null ) {
                music.setAlbum(musicRepository.findMusicById(music.getIdAlbum()));
                music.setAlbumSongs(new HashSet<>(musicRepository.findMusicByIdAlbum(music.getIdAlbum())));
            }

            List<MusicArtist> musicArtist = musicArtistRepository.findMusicArtistByIdMusic(musicId);

            music.setArtists(new HashSet<>());
            for(MusicArtist artist:musicArtist)
            {
                Artist artist1 = artistRepository.findArtistByUuid(artist.getIdArtist());
                if (artist1 != null) {
                    music.addArtist(artist1);
                }
            }
            return music;
        }
        else
            throw new MusicIdDoesNotExist(musicId);
    }

    @Override
    public CollectionModel<EntityModel<Music>> getMusicByArtistUuid(String uuid)
    {
        if(artistRepository.findArtistByUuid(uuid) != null) {
            List<EntityModel<Music>> listMusic = musicRepository.findMusicByArtists(new Artist(uuid)).stream().map(musicHateoasSimple::toModel).collect(Collectors.toList());

            if(listMusic.size() == 0)
            {
                Link parentLink = linkTo(ArtistController.class).slash("artists").slash(uuid).withRel("parent");

                return CollectionModel.of(listMusic, parentLink);
            }

            Link selfLink = linkTo(ArtistController.class).slash("artists").slash(uuid).slash("songs").withSelfRel();
            Link parentLink = linkTo(ArtistController.class).slash("artists").slash(uuid).withRel("parent");

            return CollectionModel.of(listMusic, selfLink, parentLink);
        }
        else
            throw new ArtistNotFound(uuid);
    }

    @Override
    public MusicCompleteDTO addMusic(NewMusicDTO musicDTO)
    {
        Music music = new Music(musicDTO.getName(), musicDTO.getYear(), musicDTO.getGenre(), musicDTO.getType());

        try {
            // validari pentru date

            // verificam sa nu mai existe vreo piesa cu acelasi nume pe acelasi album
            if (musicRepository.findMusicByNameAndIdAlbum(musicDTO.getName(), musicDTO.getIdAlbum()) != null) {
                throw new MusicAlreadyExists();
            }

            if (music.getGenre() == Music.GENRE.unknown) {
                throw new RuntimeException("Genre unknown. Please try again with one genre from rock, metal, pop.");
            }
            if (music.getType() == Music.TYPE.unknown) {
                throw new RuntimeException("Type unknown. Please try again with one type from single, song, album.");
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
            Date date = new Date();

            if(music.getYear() < 1950 || music.getYear() > Integer.parseInt(formatter.format(date)))
            {
                throw new RuntimeException("Year is not in range!");
            }

            if(music.getType() == Music.TYPE.song) {
                Music album = musicRepository.findMusicById(musicDTO.getIdAlbum());
                if(album != null && album.getType() == Music.TYPE.album) {
                    if (music.getGenre() == Music.GENRE.unknown) {
                        throw new RuntimeException("Genre unknown. Please try again with one genre from rock, metal, pop.");
                    }

                    music.setIdAlbum(musicDTO.getIdAlbum());
                    musicRepository.save(music);
                    music.setAlbum(musicRepository.findMusicById(music.getIdAlbum()));
                    music.setAlbumSongs(new HashSet<>(musicRepository.findMusicByIdAlbum(music.getIdAlbum())));

                    return new MusicCompleteDTO(music);
                }
                else
                {
                    throw new MusicNotFound("Album ID does not exist! Please try again with a valid album ID!");
                }
            }

            // daca e album, verific ca id album e null
            if(music.getType() == Music.TYPE.album && musicDTO.getIdAlbum() != null) {
                throw new RuntimeException("The type is album, so id album must be null!");
            }

            // add single
            if(music.getType() == Music.TYPE.single && musicDTO.getIdAlbum() != null) {
                throw new RuntimeException("The type is album, so id album must be null!");
            }

            musicRepository.save(music);

            return new MusicCompleteDTO(music);

        }
        catch (DataAccessException dataAccessException) {
            // exceptie de la jpa
            throw new JPAException(dataAccessException.getRootCause().toString());
        }
    }

    @Override
    public MusicCompleteDTO updateMusic(NewMusicDTO musicDTO)
    {
        if(musicRepository.findMusicById(musicDTO.getId()) != null)
        {
            if (musicDTO.getGenre() == Music.GENRE.unknown) {
                throw new RuntimeException("Genre unknown. Please try again with one genre from rock, metal, pop.");
            }

            if (musicDTO.getType() == Music.TYPE.unknown) {
                throw new RuntimeException("Type unknown. Please try again with one type from single, song, album.");
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
            Date date = new Date();

            if(musicDTO.getYear() < 1950 || musicDTO.getYear() > Integer.parseInt(formatter.format(date)))
            {
                throw new RuntimeException("Year is not in range!");
            }

            if(musicDTO.getType() == Music.TYPE.song) {

                if (musicRepository.findMusicById(musicDTO.getIdAlbum()) != null) {

                    Music newMusic = new Music(musicDTO.getName(), musicDTO.getYear(), musicDTO.getGenre(), musicDTO.getType());
                    newMusic.setIdAlbum(musicDTO.getIdAlbum());
                    newMusic.setAlbum(musicRepository.findMusicById(musicDTO.getIdAlbum()));
                    newMusic.setAlbumSongs(new HashSet<>(musicRepository.findMusicByIdAlbum(musicDTO.getIdAlbum())));

                    try {
                        musicRepository.save(newMusic);
                        musicRepository.deleteById(musicDTO.getId());

                        return new MusicCompleteDTO(newMusic);
                    } catch (DataAccessException dataAccessException) {
                        throw new JPAException(dataAccessException.getRootCause().toString());
                    }
                } else {
                    throw new MusicNotFound("Album id not found!");
                }
            }
            else{
                Music newMusic = new Music(musicDTO.getName(), musicDTO.getYear(), musicDTO.getGenre(), musicDTO.getType());

                try {
                    musicRepository.deleteById(musicDTO.getId());
                    musicRepository.save(newMusic);
                    return new MusicCompleteDTO(newMusic);
                } catch (DataAccessException dataAccessException) {
                    throw new JPAException(dataAccessException.getRootCause().toString());
                }
            }
        }
        throw new MusicNotFound("Id not found!");
    }

    @Override
    public MusicCompleteDTO deleteMusic(Music music)
    {
        if(musicRepository.findMusicById(music.getId()) == null)
        {
            throw new MusicNotFound();
        }

        List<MusicArtist> artistMusic = musicArtistRepository.findMusicArtistByIdMusic(music.getId());

        musicRepository.delete(music);
        MusicCompleteDTO musicDTO = new MusicCompleteDTO(music);

        if (!artistMusic.isEmpty()) {
            for(MusicArtist a:artistMusic)
            {
                musicDTO.addArtist(artistRepository.findArtistByUuid(a.getIdArtist()));
            }
        }

        return new MusicCompleteDTO(music);
    }

    @Override
    public void deleteMusicById(Integer id)
    {
        if(musicRepository.findMusicById(id) == null)
        {
            throw new MusicNotFound();
        }
        try{
            musicRepository.deleteById(id);
        }catch (DataAccessException dataAccessException)
        {
            throw new RuntimeException(dataAccessException.getRootCause());
        }
    }
}
