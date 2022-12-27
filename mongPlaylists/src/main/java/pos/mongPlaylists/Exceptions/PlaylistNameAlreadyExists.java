package pos.mongPlaylists.Exceptions;

public class PlaylistNameAlreadyExists extends RuntimeException{
    public PlaylistNameAlreadyExists(String message)
    {
        super(message);
    }
}
