package pos.mongPlaylists.View.Hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import pos.mongPlaylists.Controllers.PlaylistController;
import pos.mongPlaylists.Model.POJO.SubPlaylistPOJO;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SubPlaylistForDelete implements RepresentationModelAssembler<SubPlaylistPOJO, EntityModel<SubPlaylistPOJO>>{
    @Override
    public EntityModel<SubPlaylistPOJO> toModel(SubPlaylistPOJO outputSubPlaylistDTO) {
        return EntityModel.of(outputSubPlaylistDTO,
                linkTo(methodOn(PlaylistController.class)
                        .getAllPlaylists()).withRel("parent"));
    }
}
