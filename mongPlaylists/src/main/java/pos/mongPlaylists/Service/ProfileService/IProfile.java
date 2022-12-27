package pos.mongPlaylists.Service.ProfileService;

import org.springframework.hateoas.CollectionModel;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;
import pos.mongPlaylists.View.DTO.ProfileDTO;

import java.util.List;

public interface IProfile {
    List<ProfilePOJO> getAllProfiles();
    ProfilePOJO getProfileById(String idProfile);
    ProfilePOJO getProfileByIdUser(Integer id);

    ProfilePOJO addNewProfile(ProfileDTO profileDTO);

    ProfilePOJO updateProfileInfo(String idProfile, ProfileDTO profileDTO);
    ProfilePOJO updateProfileLikedMusic(String idProfile, ProfileDTO profileDTO, String operation);
    ProfilePOJO updateListPlaylists(String idProfile, String idPlaylist, String operation);

    ProfilePOJO deleteProfileById(String idProfile);
}
