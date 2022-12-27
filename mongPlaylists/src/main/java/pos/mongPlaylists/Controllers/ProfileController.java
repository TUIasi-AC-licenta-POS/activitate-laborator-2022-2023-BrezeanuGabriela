package pos.mongPlaylists.Controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.mongPlaylists.Exceptions.*;
import pos.mongPlaylists.Exceptions.JWTExceptions.CorruptedJwt;
import pos.mongPlaylists.Exceptions.JWTExceptions.ExpiredJwt;
import pos.mongPlaylists.Exceptions.JWTExceptions.InvalidJwt;
import pos.mongPlaylists.JWT.JwtTokenUtil;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;
import pos.mongPlaylists.Model.POJO.SubPlaylistPOJO;
import pos.mongPlaylists.Service.ProfileService.ProfileService;
import pos.mongPlaylists.SoapClient.ClientForSoap;
import pos.mongPlaylists.View.DTO.OutputProfileDTO;
import pos.mongPlaylists.View.DTO.ProfileDTO;
import pos.mongPlaylists.View.Hateoas.SubPlaylistHateoas;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {
    @Autowired
    ProfileService profileService;

    @Autowired
    SubPlaylistHateoas subPlaylistHateoas;

    @Autowired
    public JwtTokenUtil jwtTokenUtil;

    @Autowired
    ClientForSoap clientForSoap;

    public EntityModel<OutputProfileDTO> profilePOJOtoDTO(ProfilePOJO profilePOJO)
    {
        OutputProfileDTO outputProfileDTO = new OutputProfileDTO(profilePOJO);
        outputProfileDTO.setLikedMusic(profilePOJO.getLikedMusic());

        List<EntityModel<SubPlaylistPOJO>> listSubPlaylists = profilePOJO.getPlaylists()
                .stream()
                .map(p -> new SubPlaylistPOJO(p.getId(), p.getName()))
                .map(subPlaylistHateoas::toModel)
                .collect(Collectors.toList());

        outputProfileDTO.setListSubPlaylist(listSubPlaylists);

        Link selfLink = linkTo(methodOn(ProfileController.class)
                .getProfileById(profilePOJO.getId(), ""))
                .withSelfRel();

        Link parent = linkTo(methodOn(ProfileController.class)
                .getAllProfiles())
                .withRel("parent");

        return EntityModel.of(outputProfileDTO, selfLink, parent);
    }

    @GetMapping(value="/api/profiles/")
    public ResponseEntity<?> getAllProfiles()
    {
        List<ProfilePOJO> profilePOJOList = profileService.getAllProfiles();

        List<EntityModel<OutputProfileDTO>> listProfiles = profilePOJOList.stream().map(p -> profilePOJOtoDTO(p)).collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(ProfileController.class).getAllProfiles()).withSelfRel();
        CollectionModel<EntityModel<OutputProfileDTO>> collectionModel = CollectionModel.of(listProfiles, selfLink);

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @GetMapping(value="/api/profiles/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable String id,
                                            @RequestParam String field)
    {
        try{
            if(field.equals("idProfile")) {
                ProfilePOJO profilePOJO = profileService.getProfileById(id);

                return new ResponseEntity<>(profilePOJOtoDTO(profilePOJO), HttpStatus.OK);
            }
            else if(field.equals("idUser"))
            {
                try {
                    ProfilePOJO profilePOJO = profileService.getProfileByIdUser(Integer.parseInt(id));

                    return new ResponseEntity<>(profilePOJOtoDTO(profilePOJO), HttpStatus.OK);
                }catch (NumberFormatException numberFormatException)
                {
                    return new ResponseEntity<>("Id must be a number for field idUser!", HttpStatus.NOT_ACCEPTABLE);
                }
            }
            else
            {
                return new ResponseEntity<>("Path does not exist!", HttpStatus.NOT_FOUND);
            }
        }catch (ProfileNotFound profileNotFound)
        {
            return new ResponseEntity<>(profileNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value="/api/profiles/")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> addNewProfile(@RequestHeader Map<String, String> token,
                                            @RequestBody ProfileDTO profileDTO)
    {
        try{
            // verificam ca cel care adauga profile-ul este chiar user-ul in sine
            String tokenAuthenticate = token.get("authorization");

            // token-ul lipseste
            if(tokenAuthenticate == null) {
                return new ResponseEntity<>("Header-ul de autorizare lipseste!", HttpStatus.UNAUTHORIZED);
            }

            try{
                tokenAuthenticate = tokenAuthenticate.split(" ")[1];
                // daca se executa cu succes -> a trecut testul de integritate
                Claims claims = jwtTokenUtil.getAllClaimsFromToken(tokenAuthenticate);
                // testul pentru valabilitate
                if(jwtTokenUtil.isJwtExpired(claims)) {
                    return new ResponseEntity<>("Header-ul de autorizare -token login- este expirat!", HttpStatus.UNAUTHORIZED);
                }
                if(jwtTokenUtil.isIssuerKnown(claims)) {
                    return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
                }
                // se verifica id-ul - daca user-ul e cine se da de fapt
                Integer sub = (Integer) claims.get("sub");
                if(sub != profileDTO.getIdUser())
                {
                    return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                }
                try {
                    // se face cerere pentru token-ul de autorizare
                    String tokenAuthorize = clientForSoap.AuthorizeUser(tokenAuthenticate);
                    if(jwtTokenUtil.checkIfIsJwt(tokenAuthorize))
                    {
                        // s-a primit token-ul de autorizare
                        try
                        {
                            Claims claimsAuthorizate = jwtTokenUtil.getAllClaimsFromToken(tokenAuthorize);
                            if(jwtTokenUtil.isJwtExpired(claimsAuthorizate))
                            {
                                return new ResponseEntity<>("Header-ul de autorizare este expirat!", HttpStatus.UNAUTHORIZED);
                            }

                            if(jwtTokenUtil.isIssuerKnown(claimsAuthorizate)) {
                                return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
                            }

                            // a trecut testele de integritate si valabilitate

                            // se verifica rolul - daca user-ul are drept
                            List<String> roles = (List<String>) claimsAuthorizate.get("roles");
                            if(!roles.contains("client"))
                            {
                                return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                            }

                            sub = (Integer) claimsAuthorizate.get("sub");
                            if(sub != profileDTO.getIdUser())
                            {
                                return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                            }
                        }
                        // s-a primit un token corupt
                        catch (CorruptedJwt corruptedJwt)
                        {
                            return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                        }
                    }
                    else
                    {
                        String errorMessage = jwtTokenUtil.extractErrorMessage(tokenAuthorize);
                        try
                        {
                            jwtTokenUtil.extractErrorCode(errorMessage);
                        } catch (ExpiredJwt | InvalidJwt exception) {
                            return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                        }
                    }
                }
                // cererea SOAP pentru autorizare nu s-a executat cu succes
                catch (JAXBException jaxbException)
                {
                    return new ResponseEntity<>("Ne pare rau! Cererea nu a putut fi executata cu succes!", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
            // token-ul din header este corupt
            catch (CorruptedJwt corruptedJwt)
            {
                return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
            }

            System.out.println(profileDTO);
            ProfilePOJO profile = profileService.addNewProfile(profileDTO);

            return new ResponseEntity<>(profilePOJOtoDTO(profile), HttpStatus.CREATED);
        }
        catch (DuplicateProfileForUser duplicateProfile)
        {
            return new ResponseEntity<>(duplicateProfile.getMessage(), HttpStatus.CONFLICT);
        }
        catch (IncorrectRequestBody incorrectRequestBody)
        {
            // daca lipseste idUser, camp obligatoriu
            return new ResponseEntity<>(incorrectRequestBody.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
        catch (UnprocessableReqBody unprocessableReqBody)
        {
            return new ResponseEntity<>(unprocessableReqBody.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    // update la datele din profile
    @PatchMapping(value="/api/profiles/{idProfile}")
    public ResponseEntity<?> updateProfile(@RequestHeader Map<String, String> token,
                                           @PathVariable String idProfile,
                                           @RequestBody ProfileDTO profileDTO,
                                           @RequestParam(value="field") String field,
                                           @RequestParam(value="operation") Optional<String> musicOperation)
    {
        // verificam ca cel care modifica profile-ul este chiar user-ul in sine
        String tokenAuthenticate = token.get("authorization");

        // token-ul lipseste
        if(tokenAuthenticate == null) {
            return new ResponseEntity<>("Header-ul de autorizare lipseste!", HttpStatus.UNAUTHORIZED);
        }

        try{
            tokenAuthenticate = tokenAuthenticate.split(" ")[1];
            // daca se executa cu succes -> a trecut testul de integritate
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(tokenAuthenticate);
            // testul pentru valabilitate
            if(jwtTokenUtil.isJwtExpired(claims)) {
                return new ResponseEntity<>("Header-ul de autorizare este expirat!", HttpStatus.UNAUTHORIZED);
            }
            if(jwtTokenUtil.isIssuerKnown(claims)) {
                return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
            }
            try {
                // se face cerere pentru token-ul de autorizare
                String tokenAuthorize = clientForSoap.AuthorizeUser(tokenAuthenticate);
                if(jwtTokenUtil.checkIfIsJwt(tokenAuthorize))
                {
                    // s-a primit token-ul de autorizare
                    try
                    {
                        Claims claimsAuthorizate = jwtTokenUtil.getAllClaimsFromToken(tokenAuthorize);
                        if(jwtTokenUtil.isJwtExpired(claimsAuthorizate))
                        {
                            return new ResponseEntity<>("Header-ul de autorizare -token login- este expirat!", HttpStatus.UNAUTHORIZED);
                        }
                        if(jwtTokenUtil.isIssuerKnown(claimsAuthorizate)) {
                            return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
                        }

                        // a trecut testele de integritate si valabilitate
                        // se verifica rolul - daca user-ul are drept
                        List<String> roles = (List<String>) claimsAuthorizate.get("roles");
                        if(!roles.contains("client"))
                        {
                            return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                        }

                        Integer sub = (Integer) claims.get("sub");
                        if(sub != profileDTO.getIdUser())
                        {
                            return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                        }
                    }
                    // s-a primit un token corupt
                    catch (CorruptedJwt corruptedJwt)
                    {
                        return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                    }
                }
                else
                {
                    String errorMessage = jwtTokenUtil.extractErrorMessage(tokenAuthorize);
                    try
                    {
                        jwtTokenUtil.extractErrorCode(errorMessage);
                    } catch (ExpiredJwt | InvalidJwt exception) {
                        return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                    }
                }
            }
            // cererea SOAP pentru autorizare nu s-a executat cu succes
            catch (JAXBException jaxbException)
            {
                return new ResponseEntity<>("Ne pare rau! Cererea nu a putut fi executata cu succes!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        // token-ul din header este corupt
        catch (CorruptedJwt corruptedJwt)
        {
            return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
        }

        if(field.equals("info"))
        {
            try {
                ProfilePOJO profilePOJO = profileService.updateProfileInfo(idProfile, profileDTO);

                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }catch (ProfileNotFound profileNotFound)
            {
                return new ResponseEntity<>(profileNotFound.getMessage(), HttpStatus.NOT_FOUND);
            }
            catch (IncorrectRequestBody incorrectRequestBody)
            {
                return new ResponseEntity<>(incorrectRequestBody.getMessage(), HttpStatus.NOT_ACCEPTABLE);
            }
            catch (UnprocessableReqBody unprocessableReqBody)
            {
                return new ResponseEntity<>(unprocessableReqBody.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }
        if(field.equals("likedMusic"))
        {
            if(musicOperation != null && musicOperation.isPresent())
            {
                if(musicOperation.get().equals("add") || musicOperation.get().equals("remove")) {
                    try {
                        ProfilePOJO profilePOJO = profileService.updateProfileLikedMusic(idProfile, profileDTO, musicOperation.get());
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    } catch (MusicNotFound musicNotFound) {
                        return new ResponseEntity<>(musicNotFound.getMessage(), HttpStatus.CONFLICT);
                    }
                }
                else
                {
                    return new ResponseEntity<>("Path not found! Operation should be add/remove!", HttpStatus.NOT_FOUND);
                }
            }
        }

        return new ResponseEntity<>("Path not found! Field should be info/likedMusic!", HttpStatus.NOT_FOUND);
    }

    // update la datele din playlists - add or remove a playlist to a profile
    @PatchMapping(value="/api/profiles/{idProfile}/playlists/{idPlaylist}")
    public ResponseEntity<?> updatePlaylistForProfile(@RequestHeader Map<String, String> token,
                                                      @PathVariable String idProfile,
                                                      @PathVariable String idPlaylist,
                                                      @RequestParam(value="operation") String operation)
    {
        // verificam ca cel care modifica profile-ul este chiar user-ul in sine
        String tokenAuthenticate = token.get("authorization");

        // token-ul lipseste
        if(tokenAuthenticate == null) {
            return new ResponseEntity<>("Header-ul de autorizare lipseste!", HttpStatus.UNAUTHORIZED);
        }

        try{
            tokenAuthenticate = tokenAuthenticate.split(" ")[1];
            // daca se executa cu succes -> a trecut testul de integritate
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(tokenAuthenticate);
            // testul pentru valabilitate
            if(jwtTokenUtil.isJwtExpired(claims)) {
                return new ResponseEntity<>("Header-ul de autorizare este expirat!", HttpStatus.UNAUTHORIZED);
            }
            if(jwtTokenUtil.isIssuerKnown(claims)) {
                return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
            }
            try {
                // se face cerere pentru token-ul de autorizare
                String tokenAuthorize = clientForSoap.AuthorizeUser(tokenAuthenticate);
                if(jwtTokenUtil.checkIfIsJwt(tokenAuthorize))
                {
                    // s-a primit token-ul de autorizare
                    try
                    {
                        Claims claimsAuthorizate = jwtTokenUtil.getAllClaimsFromToken(tokenAuthorize);
                        if(jwtTokenUtil.isJwtExpired(claimsAuthorizate))
                        {
                            return new ResponseEntity<>("Header-ul de autorizare este expirat!", HttpStatus.UNAUTHORIZED);
                        }
                        if(jwtTokenUtil.isIssuerKnown(claimsAuthorizate)) {
                            return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
                        }

                        // a trecut testele de integritate si valabilitate
                        // se verifica rolul - daca user-ul are drept
                        List<String> roles = (List<String>) claimsAuthorizate.get("roles");
                        if(!roles.contains("client"))
                        {
                            return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                        }

                        Integer sub = (Integer) claims.get("sub");
                        ProfilePOJO profilePOJO = profileService.getProfileById(idProfile);
                        if(sub != profilePOJO.getIdUser())
                        {
                            return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                        }
                    }
                    // s-a primit un token corupt
                    catch (CorruptedJwt corruptedJwt)
                    {
                        return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                    }
                }
                else
                {
                    String errorMessage = jwtTokenUtil.extractErrorMessage(tokenAuthorize);
                    try
                    {
                        jwtTokenUtil.extractErrorCode(errorMessage);
                    } catch (ExpiredJwt | InvalidJwt exception) {
                        return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                    }
                }
            }
            // cererea SOAP pentru autorizare nu s-a executat cu succes
            catch (JAXBException jaxbException)
            {
                return new ResponseEntity<>("Ne pare rau! Cererea nu a putut fi executata cu succes!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        // token-ul din header este corupt
        catch (CorruptedJwt corruptedJwt)
        {
            return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
        }

        try {
            if(operation.equals("add") || operation.equals("remove")) {
                ProfilePOJO profilePOJO = profileService.updateListPlaylists(idProfile, idPlaylist, operation);

                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            else
            {
                return new ResponseEntity<>("Path not found. Operation should be add/remove!", HttpStatus.NOT_FOUND);
            }
        }catch (ProfileNotFound profileNotFound)
        {
            // profilul nu exista
            return new ResponseEntity<>(profileNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (PlaylistNotFound playlistNotFound)
        {
            return new ResponseEntity<>(playlistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (IdUserNotAllowed idUserNotAllowed)
        {
            return new ResponseEntity<>(idUserNotAllowed.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping(value="/api/profiles/{idProfile}")
    public ResponseEntity<?> deleteProfile(@RequestHeader Map<String, String> token,
                                            @PathVariable String idProfile)
    {
        ProfilePOJO profilePOJO;

        try{
            profilePOJO = profileService.getProfileById(idProfile);
        }
        catch (ProfileNotFound profileNotFound)
        {
            return new ResponseEntity<>(profileNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }

        // verificam ca cel care sterge profile-ul este chiar user-ul in sine
        String tokenAuthenticate = token.get("authorization");

        // token-ul lipseste
        if(tokenAuthenticate == null) {
            return new ResponseEntity<>("Header-ul de autorizare lipseste!", HttpStatus.UNAUTHORIZED);
        }

        try{
            tokenAuthenticate = tokenAuthenticate.split(" ")[1];

            // daca se executa cu succes -> a trecut testul de integritate
            Claims claims = jwtTokenUtil.getAllClaimsFromToken(tokenAuthenticate);

            // testul pentru valabilitate
            if(jwtTokenUtil.isJwtExpired(claims)) {
                return new ResponseEntity<>("Header-ul de autorizare este expirat!", HttpStatus.UNAUTHORIZED);
            }

            // testul pentru issuer
            if(jwtTokenUtil.isIssuerKnown(claims)) {
                return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
            }
            try {
                // se face cerere pentru token-ul de autorizare
                String tokenAuthorize = clientForSoap.AuthorizeUser(tokenAuthenticate);
                if(jwtTokenUtil.checkIfIsJwt(tokenAuthorize))
                {
                    // s-a primit token-ul de autorizare
                    try
                    {
                        Claims claimsAuthorizate = jwtTokenUtil.getAllClaimsFromToken(tokenAuthorize);
                        if(jwtTokenUtil.isJwtExpired(claimsAuthorizate))
                        {
                            return new ResponseEntity<>("Header-ul de autorizare este expirat!", HttpStatus.UNAUTHORIZED);
                        }
                        if(jwtTokenUtil.isIssuerKnown(claimsAuthorizate)) {
                            return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
                        }

                        // a trecut testele de integritate si valabilitate
                        // se verifica rolul - daca user-ul are drept
                        List<String> roles = (List<String>) claimsAuthorizate.get("roles");
                        if(!roles.contains("client"))
                        {
                            return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                        }

                        Integer sub = (Integer) claims.get("sub");

                        if(sub != profilePOJO.getIdUser())
                        {
                            return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                        }
                    }
                    // s-a primit un token corupt
                    catch (CorruptedJwt corruptedJwt)
                    {
                        return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                    }
                }
                else
                {
                    String errorMessage = jwtTokenUtil.extractErrorMessage(tokenAuthorize);
                    try
                    {
                        jwtTokenUtil.extractErrorCode(errorMessage);
                    } catch (ExpiredJwt | InvalidJwt exception) {
                        return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                    }
                }
            }
            // cererea SOAP pentru autorizare nu s-a executat cu succes
            catch (JAXBException jaxbException)
            {
                return new ResponseEntity<>("Ne pare rau! Cererea nu a putut fi executata cu succes!", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        // token-ul din header este corupt
        catch (CorruptedJwt corruptedJwt)
        {
            return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
        }

        // delete-ul propriu-zis
        try{
            profilePOJO = profileService.deleteProfileById(idProfile);

            OutputProfileDTO outputProfileDTO = new OutputProfileDTO(profilePOJO);
            outputProfileDTO.setLikedMusic(profilePOJO.getLikedMusic());

            List<EntityModel<SubPlaylistPOJO>> listSubPlaylists = profilePOJO.getPlaylists()
                    .stream()
                    .map(p -> new SubPlaylistPOJO(p.getId(), p.getName()))
                    .map(subPlaylistHateoas::toModel)
                    .collect(Collectors.toList());

            outputProfileDTO.setListSubPlaylist(listSubPlaylists);

            Link parent = linkTo(methodOn(ProfileController.class)
                    .getAllProfiles())
                    .withRel("parent");

            return new ResponseEntity<>(EntityModel.of(outputProfileDTO, parent), HttpStatus.OK);
        }
        catch (ProfileNotFound profileNotFound)
        {
            return new ResponseEntity<>(profileNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
