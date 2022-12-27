package com.example.spotify.Services.Music;

import com.example.spotify.Model.Music.Music;
import com.example.spotify.View.DTOs.MusicCompleteDTO;
import com.example.spotify.View.DTOs.NewMusicDTO;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import java.util.Optional;

public interface IMusic {
    MusicCompleteDTO addMusic(NewMusicDTO newMusicDTO) throws RuntimeException;

    MusicCompleteDTO deleteMusic(Music music);

    void deleteMusicById(Integer id);

    CollectionModel<EntityModel<Music>> getAllMusic();

    Music getMusicById(Integer musicID);

    CollectionModel<EntityModel<Music>> getMusicByArtistUuid(String uuid);

    MusicCompleteDTO updateMusic(NewMusicDTO newMusicDTO) throws RuntimeException;

    CollectionModel<EntityModel<Music>> getMusicByNoPage(Integer page, Optional<Integer> items_per_page);

    CollectionModel<EntityModel<Music>> getMusicByName(String name, Optional<String> match);

    CollectionModel<EntityModel<Music>> getMusicByYear(Integer year);

    CollectionModel<EntityModel<Music>> getMusicByGenre(String genre);
}
