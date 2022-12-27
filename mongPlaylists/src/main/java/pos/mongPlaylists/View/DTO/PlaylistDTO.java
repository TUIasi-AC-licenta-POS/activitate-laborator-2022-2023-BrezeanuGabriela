package pos.mongPlaylists.View.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import pos.mongPlaylists.Enums.Visibility;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PlaylistDTO {
    @Id
    private String id;

    @Setter
    @Getter
    private Integer idUser;

    @Setter
    @Getter
    private String playlistName;

    @Setter
    private String visibility;

    @Setter
    @Getter
    private List<MusicInsertDTO> songs = new ArrayList<>();

    public PlaylistDTO(Integer idUser, String playlistName, List<MusicInsertDTO> songs)
    {
        this.idUser = idUser;
        this.playlistName = playlistName;
        this.songs = songs;
    }

    public void setVisibility(String visibility)
    {
        if (visibility.equals("ppublic") || visibility.equals("pprivate") || visibility.equals("friend")) {
            this.visibility = Visibility.valueOf(visibility).toString();
        }
        else
            this.visibility = Visibility.unknown.toString();
    }
}
