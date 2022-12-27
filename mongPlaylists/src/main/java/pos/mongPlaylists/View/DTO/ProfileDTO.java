package pos.mongPlaylists.View.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class ProfileDTO {
    @Id
    private String id;

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
    private List<MusicInsertDTO> likedMusic = new ArrayList<>();

    public ProfileDTO(Integer idUser,
                      Optional<String> firstName,
                      Optional<String> lastName,
                      Optional<String> email)
    {
        this.idUser = idUser;

        if(firstName != null) {
            if (firstName.isPresent() && !firstName.isEmpty()) {
                this.firstName = Optional.of(firstName).get();
            }
        }
        if(lastName != null) {
            if(lastName.isPresent() && !lastName.isEmpty()) {
                this.lastName = Optional.of(lastName).get();
            }
        }
        if(email != null) {
            if(email.isPresent() && !email.isEmpty()) {
                this.email = Optional.of(email).get();
            }
        }
    }
}
