package pos.mongPlaylists.Service.PlaylistService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Service;
import pos.mongPlaylists.Enums.Visibility;
import pos.mongPlaylists.Exceptions.*;
import pos.mongPlaylists.Model.POJO.MusicPOJO;
import pos.mongPlaylists.Model.POJO.PlaylistPOJO;
import pos.mongPlaylists.Model.POJO.ProfilePOJO;
import pos.mongPlaylists.Model.POJO.SubPlaylistPOJO;
import pos.mongPlaylists.Model.Repositories.PlaylistRepository;
import pos.mongPlaylists.Model.Repositories.ProfileRepository;
import pos.mongPlaylists.Service.ProfileService.ProfileService;
import pos.mongPlaylists.View.DTO.MusicInsertDTO;
import pos.mongPlaylists.View.DTO.PlaylistDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService implements IPlaylist{
    @Autowired
    PlaylistRepository playlistRepository;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    ProfileService profileService;

    @Override
    public List<PlaylistPOJO> getAllPlaylists()
    {
        List<PlaylistPOJO> playlistPOJOList = playlistRepository.findAll();
        //CollectionModel<PlaylistPOJO> playlistPOJOCollectionModel = CollectionModel.of(playlistPOJOList);

        return playlistPOJOList;
    }

    public PlaylistPOJO getPlaylistById(String id)
    {
        PlaylistPOJO playlistPOJO = playlistRepository.findPlaylistPOJOById(id);
        if(playlistPOJO != null)
            return playlistPOJO;
        else
            throw new PlaylistNotFound(id);
    }

    public List<PlaylistPOJO> getAllPlaylistsByIdUser(Integer idUser)
    {
        List<PlaylistPOJO> listPlaylistsPOJO = playlistRepository.findPlaylistPOJOByIdUser(idUser);
        if(listPlaylistsPOJO.size() != 0)
            return listPlaylistsPOJO;
        else
            throw new PlaylistNotFound("User-ul " + idUser.toString() + "nu are playlist-uri!");
    }

    @Override
    public PlaylistPOJO deletePlaylistById(String id)
    {
        PlaylistPOJO playlistPOJO = playlistRepository.findPlaylistPOJOById(id);
        if(playlistPOJO != null)
        {
            playlistRepository.delete(playlistPOJO);
            return playlistPOJO;
        }
        else
            throw new PlaylistNotFound(id);
    }

    @Override
    public PlaylistPOJO addNewPlaylist(PlaylistDTO playlistDTO)
    {
        //check ca exista user-ul
        try{
            ProfilePOJO profilePOJO = profileService.getProfileByIdUser(playlistDTO.getIdUser());
        }
        catch (ProfileNotFound profileNotFound)
        {
            throw profileNotFound;
        }

        //check sa vedem daca mai e folosit numele
        List<PlaylistPOJO> userPlaylist = playlistRepository.findPlaylistPOJOByIdUser(playlistDTO.getIdUser());
        for(PlaylistPOJO playlistPOJO:userPlaylist)
        {
            if(playlistPOJO.getPlaylistName().equals(playlistDTO.getPlaylistName()))
                throw new PlaylistNameAlreadyExists("Already exists a playlist with name: " + playlistDTO.getPlaylistName());
        }

        PlaylistPOJO playlistPOJO = new PlaylistPOJO(playlistDTO.getIdUser(), playlistDTO.getPlaylistName());

        if(playlistDTO.getVisibility() != null) {
            playlistPOJO.setVisibility(playlistDTO.getVisibility());
        }
        else
        {
            throw new IncorrectRequestBody("Visibility field is missing!");
        }
        if(playlistPOJO.getVisibility() == Visibility.unknown)
        {
            throw new IncorrectRequestBody("Visibility field is incorrect! It should be ppublic/pprivate/firend");
        }

        List<MusicPOJO> musicFromSQL = new ArrayList<>();
        // preluam songs de la sql
        for (MusicInsertDTO musicInsertDTO : playlistDTO.getSongs()) {
            try{
                MusicPOJO musicPOJO = profileService.getMusicById(musicInsertDTO.getId());
                musicFromSQL.add(musicPOJO);
            }catch (MusicNotFound musicNotFound)
            {
                throw musicNotFound;
            }
        }

        playlistPOJO.setSongs(musicFromSQL);
        playlistRepository.save(playlistPOJO);

        return playlistPOJO;
    }

    @Override
    public PlaylistPOJO updatePlaylist(String idPlaylist, PlaylistDTO playlistDTO, String operation)
    {
        PlaylistPOJO oldPlaylistPOJO = playlistRepository.findPlaylistPOJOById(idPlaylist);
        if(oldPlaylistPOJO == null)
        {
            throw new PlaylistNotFound(String.format("Playlist with id - %s not found!", idPlaylist));
        }

        if(operation.equals("info")){
            ProfilePOJO profilePOJO = profileService.getProfileByIdUser(playlistDTO.getIdUser());
            profilePOJO.removePlaylist(new SubPlaylistPOJO(oldPlaylistPOJO.getId(), oldPlaylistPOJO.getPlaylistName()));

            oldPlaylistPOJO.setPlaylistName(playlistDTO.getPlaylistName());
            // idUser nu se schimba
            if(Integer.parseInt(oldPlaylistPOJO.getIdUser().toString()) != Integer.parseInt(playlistDTO.getIdUser().toString()))
            {
                throw new IdUserNotAllowed();
            }
//            oldPlaylistPOJO.setIdUser(playlistDTO.getIdUser());

            PlaylistPOJO playlistPOJO = new PlaylistPOJO(oldPlaylistPOJO.getIdUser(), oldPlaylistPOJO.getPlaylistName());

            if(playlistDTO.getVisibility() != null) {
                playlistPOJO.setVisibility(playlistDTO.getVisibility());
            }
            else
            {
                playlistPOJO.setVisibility(oldPlaylistPOJO.getVisibility().toString());
            }

            if(playlistPOJO.getVisibility() == Visibility.unknown)
            {
                throw new IncorrectRequestBody("Visibility field is incorrect! It should be ppublic/pprivate/firend");
            }

            playlistPOJO.setId(oldPlaylistPOJO.getId());
            playlistPOJO.setSongs(oldPlaylistPOJO.getSongs());

//            playlistRepository.delete(oldPlaylistPOJO);
            playlistRepository.save(playlistPOJO);

            profilePOJO.addPlaylist(new SubPlaylistPOJO(playlistPOJO.getId(), playlistPOJO.getPlaylistName()));
            profileRepository.save(profilePOJO);

            return playlistPOJO;
        }

        // update pe songs

        List<MusicPOJO> musicFromSQL = new ArrayList<>();
        // preluam songs de la sql
        for (MusicInsertDTO musicInsertDTO : playlistDTO.getSongs()) {
            try{
                MusicPOJO musicPOJO = profileService.getMusicById(musicInsertDTO.getId());
                musicFromSQL.add(musicPOJO);
            }catch (MusicNotFound musicNotFound)
            {
                throw musicNotFound;
            }
        }

        List<MusicPOJO> oldSongs = oldPlaylistPOJO.getSongs();

        if(operation.equals("add")) {
            for (MusicPOJO musicPOJO : musicFromSQL) {
                // nu introducem duplicate
                if(!oldSongs.contains(musicPOJO))
                    oldSongs.add(musicPOJO);
            }
        }
        else if(operation.equals("remove"))
        {
            for (MusicPOJO musicPOJO : musicFromSQL) {
                if (oldSongs.contains(musicPOJO))
                    oldSongs.remove(musicPOJO);
                else {
                    // daca vreau sa fac remove la o piesa care exista in sql, dar nu si in likedMusic
                    throw new MusicNotFound(musicPOJO.getId());
                }
            }
        }

        oldPlaylistPOJO.setSongs(oldSongs);

        PlaylistPOJO playlistPOJO = new PlaylistPOJO(oldPlaylistPOJO.getIdUser(), oldPlaylistPOJO.getPlaylistName());
        playlistPOJO.setId(oldPlaylistPOJO.getId());
        if(oldPlaylistPOJO.getVisibility() != null) {
            playlistPOJO.setVisibility(oldPlaylistPOJO.getVisibility().toString());
        }
        playlistPOJO.setSongs(oldPlaylistPOJO.getSongs());

        //playlistRepository.delete(oldPlaylistPOJO);
        playlistRepository.save(playlistPOJO);

        return oldPlaylistPOJO;
    }
}
