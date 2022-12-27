package com.example.spotify.Model.Music;
import com.example.spotify.Model.Artist.Artist;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface MusicRepository extends CrudRepository<Music, Integer> {
    List<Music> findMusicByName(String name);
    List<Music> findByNameLike(String pattern);

    List<Music> findMusicByYear(Integer year);

    List<Music> findMusicByGenre(Music.GENRE genre);

    List<Music> findAll();
    Music findMusicById(Integer id);

    Music findMusicByNameAndIdAlbum(String name, Integer idAlbum);

    List<Music> findMusicByArtists(Artist artist);
    //fa54adc3-daaf-462a-aa03-7d359dfdc175

    List<Music> findMusicByIdAlbum(Integer id_album);
    //List<Music> findMusicById

    @Transactional
    void deleteMusicById(Integer id);
}
