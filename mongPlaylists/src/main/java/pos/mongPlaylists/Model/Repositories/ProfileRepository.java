package pos.mongPlaylists.Model.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;

public interface ProfileRepository extends MongoRepository<ProfilePOJO, String> {
    ProfilePOJO findProfilePOJOById(String id);
    ProfilePOJO findProfilePOJOByIdUser(Integer id);
}
