package com.example.spotify.Model.Artist;

import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Music.Music;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface ArtistRepository extends CrudRepository<Artist, Integer> {
    List<Artist> findAll();
    Artist findArtistByUuid(String uuid);
    Artist findArtistByName(String name);
    Artist findArtistByNameLike(String pattern);
    @Transactional
    void deleteArtistByUuid(String uuid);
}
