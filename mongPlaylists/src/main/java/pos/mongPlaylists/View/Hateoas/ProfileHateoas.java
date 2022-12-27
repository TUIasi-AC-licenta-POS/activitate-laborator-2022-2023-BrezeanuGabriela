package pos.mongPlaylists.View.Hateoas;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import pos.mongPlaylists.Controllers.ProfileController;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;
import pos.mongPlaylists.View.DTO.OutputProfileDTO;
import pos.mongPlaylists.View.DTO.OutputSubPlaylistDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ProfileHateoas implements RepresentationModelAssembler<ProfilePOJO, EntityModel<ProfilePOJO>> {

    @Override
    public EntityModel<ProfilePOJO> toModel(ProfilePOJO profilePOJO) {

        return EntityModel.of(profilePOJO,
                WebMvcLinkBuilder.linkTo(methodOn(ProfileController.class)
                        .getProfileById(profilePOJO.getId(), ""))
                        .withSelfRel(),
                        linkTo(methodOn(ProfileController.class)
                        .getAllProfiles()).withRel("parent"));
    }
}
