package com.example.spotify.Model;

import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Artist.ArtistRepository;
import com.example.spotify.Model.Music.MusicRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;
//import spotify.enums.GENRE;
//import spotify.enums.TYPE;

@Configuration
public class LoadDatabase {

    private static final Logger log = (Logger) LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(MusicRepository repository, ArtistRepository artistRepository) {

        return args -> {
            //Music music1 = new Music("album11", 2022, Music.GENRE.pop, Music.TYPE.album);
            //log.info(music1.toString());
//            music1.setGenre("metal");
//            music1.setType("album");
//            music1.setId_album(1);
            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();
            //log.info(uuidAsString);
            Artist artist = new Artist(uuidAsString, "artist1", true);
            //log.info("Preloading " + artistRepository.save(artist));
        };
    }
}