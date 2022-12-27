package pos.mongPlaylists.Model.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import pos.mongPlaylists.Model.POJO.PlaylistPOJO;

import java.util.List;

public interface PlaylistRepository extends MongoRepository<PlaylistPOJO, String> {
    PlaylistPOJO findPlaylistPOJOById(String id);
    List<PlaylistPOJO> findPlaylistPOJOByIdUser(Integer idUser);
}
