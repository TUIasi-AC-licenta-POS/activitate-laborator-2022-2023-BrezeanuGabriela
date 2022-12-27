package pos.mongPlaylists.View.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.EntityModel;
import pos.mongPlaylists.Model.POJO.MusicPOJO;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;
import pos.mongPlaylists.Model.POJO.SubPlaylistPOJO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
public class OutputProfileDTO {
    @Setter
    @Getter
    private Integer idUser;

    @Setter
    @Getter
    private Optional<String> firstName;

    @Setter
    @Getter
    private Optional<String> lastName;

    @Setter
    @Getter
    private Optional<String> email;

    @Setter
    @Getter
    private List<MusicPOJO> likedMusic = new ArrayList<>();

    @Setter
    @Getter
    private List<EntityModel<SubPlaylistPOJO>> listSubPlaylist = new ArrayList<>();

    public OutputProfileDTO(ProfilePOJO profilePOJO)
    {
        this.idUser = profilePOJO.getIdUser();

        if(profilePOJO.getFirstName() != null)
            if(!profilePOJO.getFirstName().equals("")) {
                    this.firstName = Optional.of(profilePOJO.getFirstName());
            }
        if(profilePOJO.getLastName() != null)
            if(!profilePOJO.getLastName().equals("")) {
                    this.lastName = Optional.of(profilePOJO.getLastName());
            }
        if(profilePOJO.getEmail() != null)
            if(!profilePOJO.getEmail().equals("")) {
                    this.email = Optional.of(profilePOJO.getEmail());
            }
    }
}
