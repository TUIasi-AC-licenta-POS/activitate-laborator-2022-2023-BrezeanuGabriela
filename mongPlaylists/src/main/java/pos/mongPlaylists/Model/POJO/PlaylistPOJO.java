package pos.mongPlaylists.Model.POJO;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pos.mongPlaylists.Enums.Visibility;
import pos.mongPlaylists.View.DTO.MusicInsertDTO;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "pos_playlists")
public class PlaylistPOJO {
    @Id
    @Setter
    @Getter
    private String id;

    @Setter
    @Getter
    private Integer idUser;

    @Setter
    @Getter
    private String playlistName;

    @Getter
    private Visibility visibility;

    @Setter
    @Getter
    private List<MusicPOJO> songs = new ArrayList<>(); // favorite

    public PlaylistPOJO(Integer idUser, String playlistName)
    {
        this.idUser = idUser;
        this.playlistName = playlistName;
    }

    public void setVisibility(String visibility)
    {
       if (visibility.equals("ppublic") || visibility.equals("pprivate") || visibility.equals("friend")) {
                this.visibility = Visibility.valueOf(visibility);
       }
       else
           this.visibility = Visibility.unknown;
    }
}
