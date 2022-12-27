package pos.mongPlaylists.Exceptions;

public class UnprocessableReqBody extends RuntimeException{
    public UnprocessableReqBody()
    {
        super("Unprocessable request body!");
    }
}
