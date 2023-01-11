package pos.mongPlaylists.Controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.mongPlaylists.Enums.Visibility;
import pos.mongPlaylists.Exceptions.*;
import pos.mongPlaylists.Exceptions.JWTExceptions.CorruptedJwt;
import pos.mongPlaylists.Exceptions.JWTExceptions.ExpiredJwt;
import pos.mongPlaylists.Exceptions.JWTExceptions.InvalidJwt;
import pos.mongPlaylists.JWT.JwtTokenUtil;
import pos.mongPlaylists.Model.POJO.PlaylistPOJO;
import pos.mongPlaylists.Service.PlaylistService.PlaylistService;
import pos.mongPlaylists.SoapClient.ClientForSoap;
import pos.mongPlaylists.View.DTO.OutputPlaylistDTO;
import pos.mongPlaylists.View.DTO.PlaylistDTO;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class PlaylistController {
    @Autowired
    PlaylistService playlistService;

    @Autowired
    public JwtTokenUtil jwtTokenUtil;

    @Autowired
    ClientForSoap clientForSoap;

    public EntityModel<OutputPlaylistDTO> playlistPOJOtoDTO(PlaylistPOJO playlistPOJO)
    {
        OutputPlaylistDTO outputPlaylistDTO = new OutputPlaylistDTO(playlistPOJO.getIdUser(), playlistPOJO.getPlaylistName(), playlistPOJO.getSongs());
        if(playlistPOJO.getVisibility() != null)
        {
            outputPlaylistDTO.setVisibility(playlistPOJO.getVisibility());
        }
        Map<String, String> headers = null;

        Link selfLink = linkTo(methodOn(PlaylistController.class)
                .getPlaylistById(headers, playlistPOJO.getId(), ""))
                .withSelfRel();
        Link parent = linkTo(methodOn(PlaylistController.class)
                .getAllPlaylists())
                .withRel("parent");

        return EntityModel.of(outputPlaylistDTO, selfLink, parent);
    }

    public EntityModel<OutputPlaylistDTO> playlistPOJOtoDTOForDelete(PlaylistPOJO playlistPOJO)
    {
        OutputPlaylistDTO outputPlaylistDTO = new OutputPlaylistDTO(playlistPOJO.getIdUser(), playlistPOJO.getPlaylistName(), playlistPOJO.getSongs());
        if(playlistPOJO.getVisibility() != null)
        {
            outputPlaylistDTO.setVisibility(playlistPOJO.getVisibility());
        }
        Map<String, String> headers = null;

        Link parent = linkTo(methodOn(PlaylistController.class)
                .getAllPlaylists())
                .withRel("parent");

        return EntityModel.of(outputPlaylistDTO, parent);
    }

    @GetMapping(value="/api/playlists/")
    public ResponseEntity<?> getAllPlaylists()
    {
        List<PlaylistPOJO> playlists = playlistService.getAllPlaylists();

        List<EntityModel<OutputPlaylistDTO>> listPlaylists = playlists.stream().map(p -> playlistPOJOtoDTO(p)).collect(Collectors.toList());

        Link selfLink = linkTo(methodOn(PlaylistController.class).getAllPlaylists()).withSelfRel();
        CollectionModel<EntityModel<OutputPlaylistDTO>> collectionModel = CollectionModel.of(listPlaylists, selfLink);

        return new ResponseEntity<>(collectionModel, HttpStatus.OK);
    }

    @GetMapping(value="/api/playlists/{idPlaylist}")
    public ResponseEntity<?> getPlaylistById(
            @RequestHeader Map<String, String> token,
            @PathVariable String idPlaylist,
            @RequestParam String field)
    {
        try {
            if (field.equals("idUser")) {
                List<PlaylistPOJO> playlists = playlistService.getAllPlaylistsByIdUser(Integer.parseInt(idPlaylist));

                if (playlists.size() == 0) {
                    return new ResponseEntity<>("Playlists not found!", HttpStatus.NOT_FOUND);
                }

                Integer idUserFromPlaylists = playlists.get(0).getIdUser();

                //verificare token
                String tokenAuthenticate = token.get("authorization");

                // token-ul lipseste
                if (tokenAuthenticate == null) {
                    return new ResponseEntity<>("Header-ul de autorizare lipseste!", HttpStatus.UNAUTHORIZED);
                }

                try {
                    tokenAuthenticate = tokenAuthenticate.split(" ")[1];
                    // daca se executa cu succes -> a trecut testul de integritate
                    Claims claims = jwtTokenUtil.getAllClaimsFromToken(tokenAuthenticate);
                    // testul pentru valabilitate
                    if (jwtTokenUtil.isJwtExpired(claims)) {
                        return new ResponseEntity<>("Header-ul de autorizare -token login- este expirat!", HttpStatus.UNAUTHORIZED);
                    }
                    if (jwtTokenUtil.isIssuerKnown(claims)) {
                        return new ResponseEntity<>("Issuer not known!", HttpStatus.UNAUTHORIZED);
                    }
                    try {
                        // se face cerere pentru token-ul de autorizare
                        String tokenAuthorize = clientForSoap.AuthorizeUser(tokenAuthenticate);
                        if (jwtTokenUtil.checkIfIsJwt(tokenAuthorize)) {
                            // s-a primit token-ul de autorizare
                            try {
                                Claims claimsAuthorizate = jwtTokenUtil.getAllClaimsFromToken(tokenAuthorize);
                                if (jwtTokenUtil.isJwtExpired(claimsAuthorizate)) {
                                    return new ResponseEntity<>("Header-ul de autorizare este expirat!", HttpStatus.UNAUTHORIZED);
                                }
                                if (jwtTokenUtil.isIssuerKnown(claimsAuthorizate)) {
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
                                if (sub != idUserFromPlaylists) {
                                    return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                                }
                            }
                            // s-a primit un token corupt
                            catch (CorruptedJwt corruptedJwt) {
                                return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                            }
                        } else {
                            String errorMessage = jwtTokenUtil.extractErrorMessage(tokenAuthorize);
                            try {
                                jwtTokenUtil.extractErrorCode(errorMessage);
                            } catch (ExpiredJwt | InvalidJwt exception) {
                                return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                            }
                        }
                    }
                    // cererea SOAP pentru autorizare nu s-a executat cu succes
                    catch (JAXBException jaxbException) {
                        return new ResponseEntity<>("Ne pare rau! Cererea nu a putut fi executata cu succes!", HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
                // token-ul din header este corupt
                catch (CorruptedJwt corruptedJwt) {
                    return new ResponseEntity<>("Header-ul de autorizare este invalid!", HttpStatus.UNAUTHORIZED);
                }

                List<EntityModel<OutputPlaylistDTO>> listPlaylists = playlists.stream().map(p -> playlistPOJOtoDTO(p)).collect(Collectors.toList());

                Link selfLink = linkTo(methodOn(PlaylistController.class).getAllPlaylists()).withSelfRel();
                CollectionModel<EntityModel<OutputPlaylistDTO>> collectionModel = CollectionModel.of(listPlaylists, selfLink);

                return new ResponseEntity<>(collectionModel, HttpStatus.OK);
            }
        }catch (PlaylistNotFound playlistNotFound)
        {
            return new ResponseEntity<>(playlistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }

        // get pe idPlaylist
        try{
            PlaylistPOJO playlistPOJO = playlistService.getPlaylistById(idPlaylist);

            // daca e privat il vede doar el -> e nevoie de token
            if(playlistPOJO.getVisibility() == Visibility.pprivate)
            {
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
                                Integer sub = (Integer) claims.get("sub");
                                if(sub != playlistPOJO.getIdUser())
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
            }

            return new ResponseEntity<>(playlistPOJOtoDTO(playlistPOJO), HttpStatus.OK);
        }catch (PlaylistNotFound playlistNotFound)
        {
            return new ResponseEntity<>(playlistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value="/api/playlists/")
    public ResponseEntity<?> addNewPlaylist(
            @RequestHeader Map<String, String> token,
            @RequestBody PlaylistDTO playlistDTO)
    {
        try {
            // verificam ca cel care adauga playlist-ul este chiar user-ul in sine
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
                            if(sub != playlistDTO.getIdUser())
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

            PlaylistPOJO playlistPOJO = playlistService.addNewPlaylist(playlistDTO);
            return new ResponseEntity<>(playlistPOJOtoDTO(playlistPOJO), HttpStatus.CREATED);
        }
        catch (PlaylistNameAlreadyExists playlistNameAlreadyExists)
        {
            return new ResponseEntity<>(playlistNameAlreadyExists.getMessage(), HttpStatus.CONFLICT);
        }
        catch (MusicNotFound musicNotFound)
        {
            return new ResponseEntity<>(musicNotFound.getMessage(), HttpStatus.CONFLICT);
        }
        catch (ProfileNotFound profileNotFound)
        {
            return new ResponseEntity<>(profileNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (IncorrectRequestBody incorrectRequestBody)
        {
            return new ResponseEntity<>(incorrectRequestBody.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @PatchMapping(value="/api/playlists/{idPlaylist}")
    public ResponseEntity<?> updatePlaylist(@RequestHeader Map<String, String> token,
                                            @PathVariable String idPlaylist,
                                            @RequestBody PlaylistDTO playlistDTO,
                                            @RequestParam(value="operation") String operation)
    {
        try {
            // verificam ca cel care modifica playlist-ul este chiar user-ul in sine
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
                            System.out.println(roles);
                            if(!roles.contains("client"))
                            {
                                return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                            }

                            Integer sub = (Integer) claims.get("sub");
                            if(sub != playlistDTO.getIdUser())
                            {
                                System.out.println(sub.toString() + " " + playlistDTO.getIdUser().toString());
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

            PlaylistPOJO playlistPOJO = playlistService.updatePlaylist(idPlaylist, playlistDTO, operation);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch (PlaylistNotFound playlistNotFound)
        {
            return new ResponseEntity<>(playlistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (MusicNotFound musicNotFound)
        {
            // nu respecta constrangerile
            return new ResponseEntity<>(musicNotFound.getMessage(), HttpStatus.CONFLICT);
        }
        catch (IdUserNotAllowed idUserNotAllowed)
        {
            return new ResponseEntity<>("User not allowed", HttpStatus.FORBIDDEN);
        }
        catch (IncorrectRequestBody incorrectRequestBody)
        {
            return new ResponseEntity<>(incorrectRequestBody.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @DeleteMapping(value="/api/playlists/{idPlaylist}")
    public ResponseEntity<?> deletePlaylist(@RequestHeader Map<String, String> token,
                                            @PathVariable String idPlaylist)
    {
        PlaylistPOJO playlistPOJO;
        try{
            playlistPOJO = playlistService.getPlaylistById(idPlaylist);
        }
        catch (PlaylistNotFound playlistNotFound)
        {
            return new ResponseEntity<>(playlistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }

        try
        {
            // verificam ca cel care sterge playlist-ul este chiar user-ul in sine
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
                            System.out.println(roles);
                            if(!roles.contains("client"))
                            {
                                return new ResponseEntity<>("Forbidden!", HttpStatus.FORBIDDEN);
                            }

                            Integer sub = (Integer) claims.get("sub");
                            if(sub != playlistPOJO.getIdUser())
                            {
                                System.out.println(sub.toString() + " " + playlistPOJO.getIdUser().toString());
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

            playlistPOJO = playlistService.deletePlaylistById(idPlaylist);

            return new ResponseEntity<>(playlistPOJOtoDTOForDelete(playlistPOJO), HttpStatus.OK);
        }
        catch (PlaylistNotFound playlistNotFound)
        {
            return new ResponseEntity<>(playlistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
