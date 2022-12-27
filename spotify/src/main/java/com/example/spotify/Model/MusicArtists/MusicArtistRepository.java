package com.example.spotify.Model.MusicArtists;

import com.example.spotify.Model.Artist.Artist;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MusicArtistRepository extends CrudRepository<MusicArtist, Integer> {
    List<MusicArtist> findMusicArtistByIdMusic(Integer id_music);
    List<MusicArtist> findMusicArtistByIdArtist(String uuid);
    MusicArtist findMusicArtistByIdArtistAndAndIdMusic(String uuid, Integer id_music);
}
