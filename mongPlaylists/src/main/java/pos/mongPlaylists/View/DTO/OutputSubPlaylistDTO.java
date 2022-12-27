package pos.mongPlaylists.View.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Data
public class OutputSubPlaylistDTO {
    @Id
    private String id;

    @Setter
    @Getter
    private String name;

    public OutputSubPlaylistDTO(String name)
    {
        this.name = name;
    }
}
