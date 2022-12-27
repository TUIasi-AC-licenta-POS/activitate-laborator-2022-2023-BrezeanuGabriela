package pos.mongPlaylists.View.Hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import pos.mongPlaylists.Controllers.PlaylistController;
import pos.mongPlaylists.Controllers.ProfileController;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;
import pos.mongPlaylists.Model.POJO.SubPlaylistPOJO;
import pos.mongPlaylists.View.DTO.OutputSubPlaylistDTO;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SubPlaylistHateoas implements RepresentationModelAssembler<SubPlaylistPOJO, EntityModel<SubPlaylistPOJO>> {
    @Override
    public EntityModel<SubPlaylistPOJO> toModel(SubPlaylistPOJO outputSubPlaylistDTO) {
        Map<String, String> headers = null;
        return EntityModel.of(outputSubPlaylistDTO,
                WebMvcLinkBuilder.linkTo(methodOn(PlaylistController.class)
                                .getPlaylistById(headers, outputSubPlaylistDTO.getId(), ""))
                        .withSelfRel(),
                linkTo(methodOn(PlaylistController.class)
                        .getAllPlaylists()).withRel("parent"));
    }

}
