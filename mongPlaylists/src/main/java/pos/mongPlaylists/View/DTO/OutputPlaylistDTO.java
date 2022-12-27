package pos.mongPlaylists.View.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import pos.mongPlaylists.Enums.Visibility;
import pos.mongPlaylists.Model.POJO.MusicPOJO;

import java.util.ArrayList;
import java.util.List;

public class OutputPlaylistDTO {
    @Setter
    @Getter
    private Integer idUser;

    @Setter
    @Getter
    private Visibility visibility;

    @Setter
    @Getter
    private String playlistName;

    @Setter
    @Getter
    private List<MusicPOJO> songs = new ArrayList<>(); // favorite

    public OutputPlaylistDTO(Integer idUser, String playlistName, List<MusicPOJO> songs)
    {
        this.idUser = idUser;
        this.playlistName = playlistName;
        this.songs = songs;
    }
}
