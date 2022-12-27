package pos.mongPlaylists.Exceptions;

public class IncorrectRequestBody extends RuntimeException{
    public IncorrectRequestBody(String message)
    {
        super(message);
    }
}
