package pos.mongPlaylists.Model.POJO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Data
public class SubPlaylistPOJO {
    @Id
    private String id;

    @Setter
    @Getter
    private String name;

    public SubPlaylistPOJO(String id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
