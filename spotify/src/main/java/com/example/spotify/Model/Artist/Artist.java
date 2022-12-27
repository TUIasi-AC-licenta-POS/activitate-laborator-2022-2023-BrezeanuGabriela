package com.example.spotify.Model.Artist;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="artists")
public class Artist {
    @Id
    private String uuid;
    private String name;
    private Boolean active;

    @ManyToMany()
    @JoinTable(
            name = "music_artists",
            joinColumns = @JoinColumn(name = "id_artist"),
            inverseJoinColumns = @JoinColumn(name = "id_music")
    )
    private Set<Artist> songs = null;// = new HashSet<>();

    public Artist() {}

    public Artist(String uuid)
    {
        this.uuid = uuid;
    }

    public Artist(String uuid, String name, Boolean active)
    {
        this.uuid = uuid;
        this.name = name;
        this.active = active;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
