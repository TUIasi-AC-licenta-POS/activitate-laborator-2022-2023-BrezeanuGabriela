package com.example.spotify.Model.Music;

import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.LoadDatabase;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import spotify.enums.GENRE;
//import spotify.enums.TYPE;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="music")
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    public enum GENRE {
        rock,
        metal,
        pop,
        unknown
    };
    public enum TYPE {
        song,
        single,
        album,
        unknown
    };

    @Enumerated(EnumType.STRING)
    private GENRE genre;
    private Integer year;
    @Enumerated(EnumType.STRING)
    private TYPE type;

    @Column(name="id_album")
    private Integer idAlbum;

    private static final Logger log = (Logger) LoggerFactory.getLogger(LoadDatabase.class);

    @ManyToMany()
    @JoinTable(
            name = "music_artists",
            joinColumns = @JoinColumn(name = "id_music"),
            inverseJoinColumns = @JoinColumn(name = "id_artist")
    )
    @Setter
    private Set<Artist> artists = null;// = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "id", insertable = false, updatable = false)
    private Set<Music> albumSongs = new HashSet<>();

    @ManyToOne
    @JoinColumn(name="id",insertable = false,updatable = false)
    private Music album;

    public Music(){}

    public Music(String name, Integer year, GENRE genre, TYPE type)
    {
        this.name = name;
        this.year = year;
        this.genre = genre;
        this.type = type;
        this.artists = new HashSet<>();
        //log.info(this.toString());
    }

    public void setGenre(String genre){
        switch (genre)
        {
            case "rock" : {
                this.genre = GENRE.rock;
                break;
            }
            case "metal" : {
                this.genre = GENRE.metal;
                break;
            }
            case "pop" : {
                this.genre = GENRE.pop;
                break;
            }
            default: {
                this.genre = GENRE.unknown;
                break;
            }
        }
    }
    public GENRE getGenre() {
        return this.genre;
    }

    public void setType(String type)
    {
        switch (type) {
            case "song" : {
                this.type = TYPE.song;
                break;
            }
            case "single" : {
                this.type = TYPE.single;
                break;
            }
            case "album" : {
                this.type = TYPE.album;
                break;
            }
            default: {
                this.type = TYPE.unknown;
                break;
            }
        }
    }
    public TYPE getType() {
        return this.type;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
    public Integer getYear() {
        return this.year;
    }

    public void setIdAlbum(Integer idAlbum) {
        this.idAlbum = idAlbum;
    }
    public Integer getIdAlbum() {
        return idAlbum;
    }

    public Music getAlbum() { return album;}
    public void setAlbum(Music album) {
        this.album = album;
    }

    public Set<Music> getAlbumSongs (){ return albumSongs;}
    public void setAlbumSongs(Set<Music> albumSongs) { this.albumSongs = albumSongs; }

    public Set<Artist> getArtists() {
        return artists;
    }

    public void addArtist(Artist artist){
        System.out.println(this.artists);
        this.artists.add(artist);
    }
}
