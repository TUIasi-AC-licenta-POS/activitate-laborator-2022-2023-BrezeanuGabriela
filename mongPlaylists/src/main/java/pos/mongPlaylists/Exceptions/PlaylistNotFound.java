package pos.mongPlaylists.Exceptions;

public class PlaylistNotFound extends RuntimeException{
    public PlaylistNotFound(String message)
    {
        super(message);
    }
}
