package pos.mongPlaylists.Model.POJO;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pos.mongPlaylists.Exceptions.PlaylistNotFound;

import java.net.URL;
import java.util.*;

@Data
@NoArgsConstructor
@Document(collection = "pos_profile")
public class ProfilePOJO {
    @Id
    @Setter
    @Getter
    private String id;

    @Setter
    @Getter
    private Integer idUser;

    @Setter
    @Getter
    private String firstName;
    @Setter
    @Getter
    private String lastName;
    @Setter
    @Getter
    private String email;

    @Setter
    @Getter
    private List<MusicPOJO> likedMusic = new ArrayList<>();

    @Setter
    @Getter
    //private Set<PlaylistPOJO> playlists = new HashSet<>();
    private Set<SubPlaylistPOJO> playlists = new HashSet<>();

    public ProfilePOJO(Integer idUser, Optional<String> firstName, Optional<String> lastName, Optional<String> email)
    {
        this.idUser = idUser;

        if(firstName != null) {
            if (firstName.isPresent() && !firstName.isEmpty()) {
                this.firstName = firstName.get();
            }
        }
        if(lastName != null) {
            if(lastName.isPresent() && !lastName.isEmpty()) {
                this.lastName = lastName.get();
            }
        }
        if(email != null) {
            if(email.isPresent() && !email.isEmpty()) {
                this.email = email.get();
            }
        }
    }

    public void addPlaylist(SubPlaylistPOJO subPlaylistPOJO)
    {
        this.playlists.add(subPlaylistPOJO);
    }

    public void removePlaylist(SubPlaylistPOJO subPlaylistPOJO)
    {
        if(this.playlists.contains(subPlaylistPOJO))
        {
            this.playlists.remove(subPlaylistPOJO);
        }
        else
        {
            throw new PlaylistNotFound("Playlist not found in profile's playlists!");
        }
    }
}
