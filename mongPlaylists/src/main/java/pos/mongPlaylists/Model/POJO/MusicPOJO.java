package pos.mongPlaylists.Model.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicPOJO {
    private Integer id;
    private String name;
    private String selfLink;
}
