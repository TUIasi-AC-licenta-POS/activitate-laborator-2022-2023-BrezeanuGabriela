package pos.mongPlaylists.Service.ProfileService;

import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import pos.mongPlaylists.Exceptions.*;
import pos.mongPlaylists.Model.POJO.MusicPOJO;
import pos.mongPlaylists.Model.POJO.PlaylistPOJO;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;
import pos.mongPlaylists.Model.POJO.SubPlaylistPOJO;
import pos.mongPlaylists.Model.Repositories.PlaylistRepository;
import pos.mongPlaylists.Model.Repositories.ProfileRepository;
import pos.mongPlaylists.View.DTO.MusicInsertDTO;
import pos.mongPlaylists.View.DTO.ProfileDTO;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService implements IProfile{
    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    PlaylistRepository playlistRepository;

    private final RestTemplate restTemplate;

    public ProfileService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<ProfilePOJO> getAllProfiles()
    {
        List<ProfilePOJO> profilePOJOList = profileRepository.findAll();
        //CollectionModel<ProfilePOJO> profilePOJOCollectionModel = CollectionModel.of(profilePOJOList);

        return profilePOJOList;
    }

    @Override
    public ProfilePOJO getProfileById(String idProfile)
    {
        ProfilePOJO profilePOJO = profileRepository.findProfilePOJOById(idProfile);
        if(profilePOJO != null)
            return profilePOJO;
        else
            throw new ProfileNotFound(idProfile);
    }

    @Override
    public ProfilePOJO getProfileByIdUser(Integer id)
    {
        ProfilePOJO profilePOJO = profileRepository.findProfilePOJOByIdUser(id);
        if(profilePOJO != null)
            return profilePOJO;
        else
            throw new ProfileNotFound(id);
    }

    @Override
    public ProfilePOJO deleteProfileById(String idProfile)
    {
        ProfilePOJO profilePOJO = profileRepository.findProfilePOJOById(idProfile);

        if(profilePOJO != null) {
            profileRepository.delete(profilePOJO);
            return profilePOJO;
        }
        else
            throw new ProfileNotFound(idProfile);
    }

    public Boolean checkProfileInformation(ProfileDTO profileDTO)
    {
        if(profileDTO.getFirstName() != null)
        {
            if(profileDTO.getFirstName().isPresent())
            {
                if((profileDTO.getFirstName().get().indexOf(".") != -1) ||
                        (profileDTO.getFirstName().get().indexOf("\'") != -1))
                {
                    return false;
                }
            }
        }
        if(profileDTO.getLastName() != null) {
            if (profileDTO.getLastName().isPresent()) {
                if((profileDTO.getLastName().get().indexOf(".") != -1) ||
                        (profileDTO.getLastName().get().indexOf("\'") != -1))
                {
                    return false;
                }
            }
        }
        if(profileDTO.getEmail() != null) {
            if (profileDTO.getEmail().isPresent()) {
                if (profileDTO.getEmail().get().indexOf("\'") != -1)
                {
                    return false;
                }
//                if (profileDTO.getEmail().get().indexOf("@") == -1)
//                {
//                    return false;
//                }
            }
        }
        return true;
    }

    @Override
    public ProfilePOJO addNewProfile(ProfileDTO profileDTO)
    {
        if(profileDTO.getIdUser() == null) {
            throw new IncorrectRequestBody("IdUser not found in request body!");
        }
        if(profileRepository.findProfilePOJOByIdUser(profileDTO.getIdUser()) == null) {
            // validarea datelor - firstName, lastName, email
            if(!checkProfileInformation(profileDTO))
            {
                throw new UnprocessableReqBody();
            }

            // cand e creat profilul, e normal ca likedMusic sa fie o lista vida
            if(profileDTO.getLikedMusic().size() != 0)
            {
                throw new UnprocessableReqBody();
            }

            ProfilePOJO profilePOJO = new ProfilePOJO(profileDTO.getIdUser(), profileDTO.getFirstName(), profileDTO.getLastName(), profileDTO.getEmail());
            profileRepository.save(profilePOJO);
            return profilePOJO;
        }
        else
        {
            throw new DuplicateProfileForUser(profileDTO.getIdUser());
        }
    }

    @Override
    public ProfilePOJO updateProfileInfo(String idProfile, ProfileDTO profileDTO)
    {
        if(profileDTO.getIdUser() == null) {
            throw new IncorrectRequestBody("IdUser not found in request body!");
        }

        ProfilePOJO oldProfile = profileRepository.findProfilePOJOById(idProfile);
        if(oldProfile == null)
        {
            throw new ProfileNotFound(idProfile);
        }

        // validarea datelor - firstName, lastName, email
        if(!checkProfileInformation(profileDTO))
        {
            throw new UnprocessableReqBody();
        }

        // nu e update pentru likedMusic pentru a avea music aici
        if(profileDTO.getLikedMusic().size() != 0)
        {
            throw new UnprocessableReqBody();
        }

        ProfilePOJO profilePOJO = new ProfilePOJO(profileDTO.getIdUser(), profileDTO.getFirstName(), profileDTO.getLastName(), profileDTO.getEmail());
        profilePOJO.setLikedMusic(oldProfile.getLikedMusic());

//        profileRepository.delete(oldProfile);

        profilePOJO.setId(oldProfile.getId());
        profileRepository.save(profilePOJO);

        return profilePOJO;
    }

    @Override
    public ProfilePOJO updateProfileLikedMusic(String idProfile, ProfileDTO profileDTO, String musicOperation) {
        ProfilePOJO oldProfile = profileRepository.findProfilePOJOById(idProfile);
        if(oldProfile == null)
        {
            throw new ProfileNotFound(idProfile);
        }

        List<MusicPOJO> likedMusic = oldProfile.getLikedMusic();

        List<MusicPOJO> musicFromSQL = new ArrayList<>();
        // preluam songs de la sql
        for (MusicInsertDTO musicInsertDTO : profileDTO.getLikedMusic()) {
            try{
                MusicPOJO musicPOJO = this.getMusicById(musicInsertDTO.getId());
                musicFromSQL.add(musicPOJO);
            }catch (MusicNotFound musicNotFound)
            {
                throw musicNotFound;
            }
        }

        if(musicOperation.equals("add")) {
            for (MusicPOJO musicPOJO : musicFromSQL) {
                // nu introducem duplicate
                if(!likedMusic.contains(musicPOJO))
                    likedMusic.add(musicPOJO);
            }
        }
        else if(musicOperation.equals("remove"))
        {
            for (MusicPOJO musicPOJO : musicFromSQL) {
                if (likedMusic.contains(musicPOJO))
                    likedMusic.remove(musicPOJO);
                else {
                    // daca vreau sa fac remove la o piesa care exista in sql, dar nu si in likedMusic
                    throw new MusicNotFound(musicPOJO.getId());
                }
            }
        }

//        oldProfile.setLikedMusic(likedMusic);
        ProfilePOJO profilePOJO = new ProfilePOJO(profileDTO.getIdUser(), profileDTO.getFirstName(), profileDTO.getLastName(), profileDTO.getEmail());
        profilePOJO.setLikedMusic(likedMusic);
        profilePOJO.setId(oldProfile.getId());
        profilePOJO.setPlaylists(oldProfile.getPlaylists());

//        profileRepository.delete(oldProfile);
        profileRepository.save(oldProfile);

        return oldProfile;
    }

    @Override
    public ProfilePOJO updateListPlaylists(String idProfile, String idPlaylist, String operation)
    {
        ProfilePOJO profile = profileRepository.findProfilePOJOById(idProfile);
        if(profile == null)
        {
            throw new ProfileNotFound(idProfile);
        }
        System.out.println(profile);

        // verificam ca exista playlist-ul
        PlaylistPOJO playlistPOJO = playlistRepository.findPlaylistPOJOById(idPlaylist);
        if(playlistPOJO == null)
        {
            throw new PlaylistNotFound(String.format("Playlist with id - %s not found!", idPlaylist));
        }

        // user-ul vrea sa isi adauge un playlist care nu ii apartine
        if(Integer.parseInt(profile.getIdUser().toString()) != Integer.parseInt(playlistPOJO.getIdUser().toString()))
        {
            throw new IdUserNotAllowed();
        }

        if(operation.equals("add"))
        {
            profile.addPlaylist(new SubPlaylistPOJO(playlistPOJO.getId(), playlistPOJO.getPlaylistName()));
        }

        else if(operation.equals("remove"))
        {
            try {
                profile.removePlaylist(new SubPlaylistPOJO(playlistPOJO.getId(), playlistPOJO.getPlaylistName()));
            } catch (PlaylistNotFound playlistNotFound) {
                // incerc sa elimin ceva ce nici nu exista
                throw playlistNotFound;
            }
        }
        System.out.println(profile);
        profileRepository.save(profile);
        return profile;
    }

    public MusicPOJO getMusicById(Integer id) {
        try{
            String url = "http://127.0.0.1:8080/api/songcollection/songs/"+id;
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(new HttpHeaders()),
                    Object.class);
//            System.out.println(response.getBody());
            JSONObject musicDTO = new JSONObject(new Gson().toJson(response.getBody()));
//            Integer idMusic = musicDTO.getInt("id");
            String name = musicDTO.getString("name");
            String selfLink = musicDTO.getJSONObject("_links").getJSONObject("self").getString("href");
            MusicPOJO musicPOJO = new MusicPOJO(id, name, selfLink);
            return musicPOJO;
        }
        catch (HttpClientErrorException httpClientErrorException)
        {
            throw new MusicNotFound(id);
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
