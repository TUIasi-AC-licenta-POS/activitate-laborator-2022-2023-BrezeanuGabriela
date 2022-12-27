package com.example.spotify.Controller;

import com.example.spotify.Exceptions.ArtistNotFound;
import com.example.spotify.Exceptions.JPAException;
import com.example.spotify.Exceptions.JWTExceptions.CorruptedJwt;
import com.example.spotify.Exceptions.JWTExceptions.ExpiredJwt;
import com.example.spotify.Exceptions.JWTExceptions.InvalidJwt;
import com.example.spotify.JWT.JwtTokenUtil;
import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Artist.ArtistRepository;
import com.example.spotify.Model.Music.Music;
import com.example.spotify.Model.Music.MusicRepository;
import com.example.spotify.Model.MusicArtists.MusicArtist;
import com.example.spotify.Model.MusicArtists.MusicArtistIds;
import com.example.spotify.Model.MusicArtists.MusicArtistRepository;
import com.example.spotify.Services.Artist.ArtistService;
import com.example.spotify.Services.Artist.IArtist;
import com.example.spotify.Services.Music.IMusic;
import com.example.spotify.SoapClient.ClientForSoap;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ArtistController {
    @Autowired
    private MusicArtistRepository musicArtistRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private final IArtist artistService;

    @Autowired
    public JwtTokenUtil jwtTokenUtil;

    @Autowired
    ClientForSoap clientForSoap;

    ArtistController(IArtist artistService) {
        this.artistService = artistService;
    }

    /////////////////////////////////   GET     //////////////////////////////////

    @GetMapping("/api/songcollection/artists/")
    public ResponseEntity<?> getAllArtists(
            @RequestParam(value="name") Optional<String> name,
            @RequestParam(value="match") Optional<String> match)
    {
        try {
            if (name.isPresent()) {
                return new ResponseEntity<>(artistService.getArtistByName(name.get(), match), HttpStatus.OK);
            }

            return new ResponseEntity<>(artistService.getAllArtists(), HttpStatus.OK);
        }
        catch (RuntimeException runtimeException)
        {
            return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/api/songcollection/artists/{artistUuid}", produces = "application/hal+json")
    public ResponseEntity<?> getArtistByUuid(@PathVariable final String artistUuid)
    {
        try {
            EntityModel<Artist> artist = artistService.getArtistByUuid(artistUuid);
            return new ResponseEntity<>(artist, HttpStatus.OK);
        }catch (ArtistNotFound artistNotFound) {
            return new ResponseEntity<>(artistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /////////////////////////////////     PUT     //////////////////////////////////
    // metoda pentru tabela de join
    @PutMapping(value = "/api/songcollection/artists/{uuid}/songs/{id}", produces = "application/hal+json")
    public ResponseEntity<?> addMusicArtist(
                                            @PathVariable String uuid,
                                            @PathVariable Integer id,
                                            @RequestParam(value="operation") String operation,
                                            @RequestBody Optional<MusicArtistIds> musicArtistIds)
    {
        // se determina operatia dorita
        if(operation.equals("add")) {
            // verific ca exista artistul
            if (artistService.getArtistByUuid(uuid) != null) {
                // verific ca nu se incearca sa se introduca un duplicat in tabelul de join
                if (musicArtistRepository.findMusicArtistByIdArtistAndAndIdMusic(uuid, id) != null) {
                    return new ResponseEntity(HttpStatus.CONFLICT);
                }

                // id ul e verificat din music de unde vine cererea
                // se face insertul
                MusicArtistIds musicArtistIds1 = new MusicArtistIds(id, uuid);
                MusicArtist musicArtist = new MusicArtist(musicArtistIds1);
                musicArtistRepository.save(musicArtist);

                return new ResponseEntity(musicArtistIds1, HttpStatus.CREATED);
            }
            else {
                return new ResponseEntity("Artist not found!", HttpStatus.NOT_FOUND);
            }
        }
        else if(operation.equals("update"))
        {
             // uuid ul din path e cel vechi, iar cel din body e cel nou
            System.out.println(uuid);
            if(artistService.getArtistByUuid(musicArtistIds.get().getIdArtist()) != null)
            {
                if (musicArtistRepository.findMusicArtistByIdArtistAndAndIdMusic(musicArtistIds.get().getIdArtist(), id) != null) {
                    //throw new RuntimeException("Duplicate key!");
                    return new ResponseEntity(HttpStatus.CONFLICT);
                }
                    MusicArtist newMusicArtist = new MusicArtist(musicArtistIds.get());
                MusicArtist oldMusicArtist = new MusicArtist(new MusicArtistIds(id, uuid));
                musicArtistRepository.delete(oldMusicArtist);
                musicArtistRepository.save(newMusicArtist);

                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            else {
                return new ResponseEntity<>("Artist not found!", HttpStatus.NOT_FOUND);
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/api/songcollection/artists/{uuid}", produces = "application/hal+json")
    public ResponseEntity<?> addOrUpdateArtist(
            @RequestHeader Map<String, String> token,
            @PathVariable final String uuid, @RequestBody Artist artist)
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
                        List<String> roles = (List<String>) claimsAuthorizate.get("roles");
                        if(!roles.contains("content_manager"))
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

        // se determina operatia dorita
        try {
            // create
            if (artistRepository.findArtistByUuid(uuid) == null) {
                if(!uuid.equals(artist.getUuid()))
                {
                    return new ResponseEntity("Uuid already exists!", HttpStatus.CONFLICT);
                }

                artist.setUuid(uuid);
                EntityModel<Artist> newArtist = artistService.addArtist(artist);

                return new ResponseEntity(newArtist, HttpStatus.CREATED);
            }
            // update
            else {
                // uuid ul din path e cel vechi, iar cel din body e cel nou
                EntityModel<Artist> updatedArtist = artistService.updateArtist(artist, uuid);

                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
            //return new ResponseEntity<>("bla bla bla", HttpStatus.NO_CONTENT);
        }
        // exceptii datorate constrangerilor SQL
        catch (JPAException jpaException) {
            return new ResponseEntity<>(jpaException.getMessage(), HttpStatus.CONFLICT);
        }
        catch (RuntimeException runtimeException) {
            return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    //http://localhost:8080/api/songcollection/artists/fa54adc3-daaf-462a-aa03-7d359dfdc175/songs

    //////////////////////////////////////      DELETE         //////////////////////
    @DeleteMapping(value = "api/songcollection/artists/{uuid}", produces = "application/hal+json")
    public ResponseEntity<?> deleteArtist(
            @RequestHeader Map<String, String> token,
            @PathVariable final String uuid)
    {
        String tokenAuthenticate = token.get("authorization");

        // token-ul lipseste
        if(tokenAuthenticate == null) {
            return new ResponseEntity<>("Header-ul de autorizare lipseste!", HttpStatus.UNAUTHORIZED);
        }

        try{
            if(tokenAuthenticate.split(" ").length != 2)
                return new ResponseEntity<>("Header-ul de autorizare nu respecta formatul!", HttpStatus.UNAUTHORIZED);

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
                        if(!roles.contains("content_manager"))
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

        try{
            EntityModel entityModel = artistService.deleteArtist(uuid);
            return new ResponseEntity<>(entityModel, HttpStatus.OK);
        }
        catch (JPAException jpaException)
        {
            return new ResponseEntity<>(jpaException.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
        catch (RuntimeException runtimeException)
        {
            return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
