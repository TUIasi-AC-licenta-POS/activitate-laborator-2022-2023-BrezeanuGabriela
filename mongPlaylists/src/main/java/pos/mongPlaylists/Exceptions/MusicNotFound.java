package pos.mongPlaylists.Exceptions;

public class MusicNotFound extends RuntimeException{
    public MusicNotFound(Integer id)
    {
        super(String.format("Music with id - %s not found!", id));
    }

    public MusicNotFound()
    {
        super("Music not found");
    }

    public MusicNotFound(String message) {
        super(message);
    }
}
