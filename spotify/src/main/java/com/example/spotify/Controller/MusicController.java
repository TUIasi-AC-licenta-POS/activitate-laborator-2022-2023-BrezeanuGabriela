package com.example.spotify.Controller;

import com.example.spotify.Exceptions.*;
import com.example.spotify.Exceptions.JWTExceptions.CorruptedJwt;
import com.example.spotify.Exceptions.JWTExceptions.ExpiredJwt;
import com.example.spotify.Exceptions.JWTExceptions.InvalidJwt;
import com.example.spotify.JWT.JwtTokenUtil;
import com.example.spotify.Model.Artist.Artist;
import com.example.spotify.Model.Artist.ArtistRepository;
import com.example.spotify.Model.Music.Music;
import com.example.spotify.Model.MusicArtists.MusicArtist;
import com.example.spotify.Model.MusicArtists.MusicArtistIds;
import com.example.spotify.Model.MusicArtists.MusicArtistRepository;
import com.example.spotify.Services.Music.IMusic;
import com.example.spotify.SoapClient.ClientForSoap;
import com.example.spotify.View.DTOs.MusicCompleteDTO;
import com.example.spotify.View.DTOs.MusicWithAlbumSongsDTO;
import com.example.spotify.View.DTOs.SimpleMusicDTO;
import com.example.spotify.View.Hateoas.*;
import com.example.spotify.View.DTOs.NewMusicDTO;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class MusicController {
    private final RestTemplate restTemplate;

    @Autowired
    private MusicArtistRepository musicArtistRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private final IMusic musicService;

    @Autowired
    private MusicHateoasVerySimple musicHateoasVerySimple;

    @Autowired
    private ArtistHateoasSimple artistHateoasSimple;

    @Autowired
    public JwtTokenUtil jwtTokenUtil;

    @Autowired
    ClientForSoap clientForSoap;

    MusicController(IMusic musicService)
    {
        this.musicService = musicService;
        this.restTemplate = new RestTemplate();
    }

    ///////////////////////////////////////////////////////////////     GET     /////////////////////////////////////////
    @GetMapping("/api/songcollection/songs/")
    //@CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> getAllMusic(@RequestParam(value = "page")Optional<Integer> page,
                                         @RequestParam(value="items_per_page") Optional<Integer> items_per_page,
                                         @RequestParam(value="name") Optional<String> name,
                                         @RequestParam(value="match") Optional<String> match,
                                         @RequestParam(value="genre") Optional<String> genre,
                                         @RequestParam(value="year") Optional<Integer> year) {
        try{
            if(page.isPresent())
            {
                return new ResponseEntity<>(musicService.getMusicByNoPage(page.get(), items_per_page), HttpStatus.OK);
            }

            if(name.isPresent())
            {
                return new ResponseEntity<>(musicService.getMusicByName(name.get(), match), HttpStatus.OK);
            }

            if(year.isPresent())
            {
                return new ResponseEntity<>(musicService.getMusicByYear(year.get()), HttpStatus.OK);
            }

            if(genre.isPresent())
            {
                return new ResponseEntity<>(musicService.getMusicByGenre(genre.get()), HttpStatus.OK);
            }

            /// cazult default fara query param
            CollectionModel<EntityModel<Music>> collectionModel = musicService.getAllMusic();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Access-Control-Allow-Origin", "*");

            return new ResponseEntity<>(collectionModel, HttpStatus.OK);
        }catch (MusicNotFound musicNotFound)
        {
            return new ResponseEntity<>(musicNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (RuntimeException runtimeException)
        {
            return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/api/songcollection/songs/{musicId}", produces = "application/hal+json")
    public ResponseEntity<?> getMusicById(@PathVariable final Integer musicId)
    {
        try{
            Music music = musicService.getMusicById(musicId);

            // afiseaza informatiile complete despre piesa
            if(music.getIdAlbum() != null) {
                MusicWithAlbumSongsDTO musicWithAlbumSongsDTO = new MusicWithAlbumSongsDTO(music);

                // get dto for album songs and save them
                List<EntityModel<SimpleMusicDTO>> listMusic = music.getAlbumSongs()
                                                                .stream()
                                                                .map(m -> new SimpleMusicDTO(m.getId(), m.getName(), m.getGenre()))
                                                                .map(musicHateoasVerySimple::toModel)
                                                                .collect(Collectors.toList());
                musicWithAlbumSongsDTO.setAlbumSongs(listMusic);

                // set hateoas for album
                musicWithAlbumSongsDTO.setAlbum(
                        musicHateoasVerySimple.toModel(
                                new SimpleMusicDTO(music.getIdAlbum(),music.getAlbum().getName(), music.getGenre())
                        )
                );

                // set hateoas for artist
                if(!music.getArtists().isEmpty()) {
                    for (Artist artist : music.getArtists()) {
                        music.addArtist(artist);
                        musicWithAlbumSongsDTO.addArtist(artistHateoasSimple.toModel(artist));
                    }
                }

                return new ResponseEntity<>(new MusicHateoasMultipleDetailsComp().toModel(musicWithAlbumSongsDTO), HttpStatus.OK);
            }
//             este album/single
            else
            {
                MusicWithAlbumSongsDTO musicWithAlbumSongsDTO = new MusicWithAlbumSongsDTO(music);
                System.out.println(music.getName());
                if(!music.getArtists().isEmpty()) {
                    for (Artist artist : music.getArtists()) {
                        music.addArtist(artist);
                        musicWithAlbumSongsDTO.addArtist(artistHateoasSimple.toModel(artist));
                    }
                }
                return new ResponseEntity<>(new MusicHateoasMultipleDetailsComp().toModel(musicWithAlbumSongsDTO), HttpStatus.OK);
            }

        }catch (MusicIdDoesNotExist musicIdDoesNotExist)
        {
            return new ResponseEntity<String>(musicIdDoesNotExist.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/api/songcollection/artists/{artistUuid}/songs", produces = "application/hal+json")
    public ResponseEntity<?> getMusicByArtistUuid(@PathVariable final String artistUuid)
    {
        try{
            return new ResponseEntity<>(musicService.getMusicByArtistUuid(artistUuid), HttpStatus.OK);
        }catch (ArtistNotFound artistNotFound)
        {
            return new ResponseEntity<>(artistNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    ///////////////////////////////////////////////////////////////     POST       /////////////////////////////////////////

    @PostMapping(value = "api/songcollection/songs")
    public ResponseEntity<?> addMusic(
            @RequestHeader Map<String, String> token,
            @RequestBody NewMusicDTO musicDTO)
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

        try{
            MusicCompleteDTO createdMusic = musicService.addMusic(musicDTO);

            try {
                // add entry in join table music_artist for every artist
                String url;
                List<String> artistUuids = musicDTO.getIdsArtist();

                // asta e pentru varianta rapida in modul dev, in mod normal stiu ca ar trebui facute manual fiecare cerere
                // asa proiectul e si server si client, dar se poate comenta ff usor for-ul :D
                for (String uuid : artistUuids)
                {
                        url = String.format("http://127.0.0.1:8080/api/songcollection/artists/%s/songs/%d?operation=add", uuid, createdMusic.getId());
                        HttpHeaders headers = new HttpHeaders();
                        HttpEntity<?> request = new HttpEntity<>(headers);
                        ResponseEntity<?> response = this.restTemplate.exchange(
                                url,
                                HttpMethod.PUT,
                                request,
                                MusicArtist.class);
                        //System.out.println(response.getStatusCode());
                }

                // s-a inserat cu succes
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Location", String.format("http://localhost:8080/api/songcollection/songs/%s", createdMusic.getId()));
                return new ResponseEntity<>(new MusicHateoasComplete().toModel(createdMusic), headers, HttpStatus.CREATED);

            }catch (RuntimeException runtimeException)
            {
//                System.out.println(runtimeException.getMessage());
                Music music = new Music(musicDTO.getName(), musicDTO.getYear(), musicDTO.getGenre(), musicDTO.getType());
                music.setId(createdMusic.getId());
                musicService.deleteMusic(music);

                if(runtimeException.getMessage().split(" : ")[0].equals("409"))
                {
                    return new ResponseEntity<>("Duplicate uuid for artist!", HttpStatus.CONFLICT);
                }
                else if(runtimeException.getMessage().split(" : ")[0].equals(("404")))
                {
                    return new ResponseEntity<>("Artist not found!", HttpStatus.CONFLICT);
                }
                return new ResponseEntity<>("ana are mere", HttpStatus.NOT_FOUND);
            }
        }catch (JPAException jpaException)
        {
            // reprezentarea nu corespunde cu cea din tabel sau orice eroare ce tine de jpa
            return new ResponseEntity<>(jpaException.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
        catch (MusicNotFound musicNotFound)
        {
            return new ResponseEntity<>(musicNotFound.getMessage(), HttpStatus.CONFLICT);
        }
        catch (MusicAlreadyExists musicAlreadyExists)
        {
            return new ResponseEntity<>("Name already exists in this album!", HttpStatus.CONFLICT);
        }
        catch (RuntimeException runtimeException)
        {
            // cerere sintactic ok, semantic nu
            return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    ///////////////////////////////////////////////////////////////     PUT     /////////////////////////////////////////

    @PutMapping(value = "api/songcollection/songs/{id}", produces = "application/hal+json")
    public ResponseEntity updateMusic(
            @RequestHeader Map<String, String> token,
            @PathVariable final Integer id,
            @RequestBody NewMusicDTO musicDTO)
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

        // se incearca update-ul
        musicDTO.setId(id);
        try {
            MusicCompleteDTO musicCompleteDTO = musicService.updateMusic(musicDTO);
            System.out.println(musicCompleteDTO.getArtists());

            // update join table
            try {
                String url = "http://127.0.0.1:8080/api/songcollection/artists/{uuid}/songs/{id}";

                List<String> artistUuids = musicDTO.getIdsArtist();
                System.out.println(artistUuids);

                // pentru fiecare artist facem insert si in tabela de join cu noul id de la music
                for (String uuid : artistUuids) {
                    url = String.format("http://127.0.0.1:8080/api/songcollection/artists/%s/songs/%d?operation=update", uuid, musicCompleteDTO.getId());
                    HttpEntity<MusicArtist> request = new HttpEntity(new MusicArtistIds(musicCompleteDTO.getId(), uuid));
                    ResponseEntity<?> response = this.restTemplate.exchange(
                            url,
                            HttpMethod.PUT,
                            request,
                            MusicArtist.class);
                    System.out.println(response.getStatusCode());
                }
            }catch (RuntimeException runtimeException)
            {
                System.out.println(runtimeException.getMessage());
                Music music = new Music(musicDTO.getName(), musicDTO.getYear(), musicDTO.getGenre(), musicDTO.getType());
                music.setId(musicCompleteDTO.getId());
                musicService.deleteMusic(music);

                if(runtimeException.getMessage().split(" : ")[0].equals(("404")))
                {
                    return new ResponseEntity<>("Artist not found!", HttpStatus.NOT_FOUND);
                }
                if(runtimeException.getMessage().split(" : ")[0].equals("409"))
                {
                    return new ResponseEntity<>("Duplicate uuid for artist!", HttpStatus.CONFLICT);
                }

                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch (JPAException jpaException)
        {
            return new ResponseEntity<>(jpaException.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
        catch (MusicNotFound musicNotFound)
        {
            return new ResponseEntity<>(musicNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (RuntimeException runtimeException)
        {
            return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    ///////////////////////////////////////////////////////////////     DELETE      /////////////////////////////////////////

    @DeleteMapping(value = "api/songcollection/songs/{id}", produces = "application/hal+json")
    public ResponseEntity<?> deleteByID(
            @RequestHeader Map<String, String> token,
            @PathVariable final Integer id)
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

        // se incearca delete-ul
        try{
            // facem o copie la obiect
            Music music = musicService.getMusicById(id);
            MusicCompleteDTO musicCompleteDTO = new MusicCompleteDTO(music);

            List<MusicArtist> musicArtistsList = musicArtistRepository.findMusicArtistByIdMusic(id);
            if(!musicArtistsList.isEmpty()) {
                for(MusicArtist musicArtist:musicArtistsList) {
                    Artist artist = artistRepository.findArtistByUuid(musicArtist.getIdArtist());
                    musicCompleteDTO.addArtist(artist);
                }
            }

            musicService.deleteMusicById(id);

            if(music.getIdAlbum() != null)
            {
                MusicWithAlbumSongsDTO musicWithAlbumSongsDTO = new MusicWithAlbumSongsDTO(music);
                List<EntityModel<SimpleMusicDTO>> listMusic = music.getAlbumSongs()
                        .stream()
                        .map(m -> new SimpleMusicDTO(m.getId(), m.getName(), m.getGenre()))
                        .map(musicHateoasVerySimple::toModel)
                        .collect(Collectors.toList());
                musicWithAlbumSongsDTO.setAlbumSongs(listMusic);

                // set hateoas for album
                musicWithAlbumSongsDTO.setAlbum(
                        musicHateoasVerySimple.toModel(
                                new SimpleMusicDTO(music.getId(),music.getAlbum().getName(), music.getGenre())
                        )
                );

                // set hateoas for artist
                if(!music.getArtists().isEmpty()) {
                    for (Artist artist : music.getArtists()) {
                        music.addArtist(artist);
                        musicWithAlbumSongsDTO.addArtist(artistHateoasSimple.toModel(artist));
                    }
                }
                return new ResponseEntity<>(new MusicHateoasMultipleDetailsComp().toModel(musicWithAlbumSongsDTO), HttpStatus.OK);
            }
            // este album/single
            else
            {
                return new ResponseEntity<>(new MusicHateoasSimple().toModel(music), HttpStatus.OK);
            }
        }catch (MusicIdDoesNotExist musicNotFound)
        {
            return new ResponseEntity<>(musicNotFound.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (RuntimeException runtimeException)
        {
            return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.CONFLICT);
        }
    }
}
