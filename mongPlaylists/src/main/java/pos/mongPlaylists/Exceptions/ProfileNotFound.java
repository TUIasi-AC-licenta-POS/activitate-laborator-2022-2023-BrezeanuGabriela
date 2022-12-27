package pos.mongPlaylists.Exceptions;

public class ProfileNotFound extends RuntimeException{
    public ProfileNotFound(String id)
    {
        super(String.format("Profile with id - %s not found!", id));
    }

    public ProfileNotFound(Integer id)
    {
        super(String.format("Profile with id - %d not found!", id));
    }
}
