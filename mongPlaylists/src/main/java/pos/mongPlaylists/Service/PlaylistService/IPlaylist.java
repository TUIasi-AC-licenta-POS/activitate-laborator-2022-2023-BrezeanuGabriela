package pos.mongPlaylists.Service.PlaylistService;

import org.springframework.hateoas.CollectionModel;
import pos.mongPlaylists.Model.POJO.PlaylistPOJO;
import pos.mongPlaylists.View.DTO.PlaylistDTO;

import java.util.List;

public interface IPlaylist {
    List<PlaylistPOJO> getAllPlaylists();
    List<PlaylistPOJO> getAllPlaylistsByIdUser(Integer idUser);

    PlaylistPOJO addNewPlaylist(PlaylistDTO playlistDTO);

    PlaylistPOJO updatePlaylist(String idPlaylist, PlaylistDTO playlistDTO, String operation);

    PlaylistPOJO deletePlaylistById(String id);
}
