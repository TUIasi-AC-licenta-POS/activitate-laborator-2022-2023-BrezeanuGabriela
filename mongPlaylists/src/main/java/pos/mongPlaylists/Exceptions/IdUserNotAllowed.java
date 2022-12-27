package pos.mongPlaylists.Exceptions;

public class IdUserNotAllowed extends  RuntimeException{
    public IdUserNotAllowed()
    {
        super("User has no permission for playlist!");
    }
}
