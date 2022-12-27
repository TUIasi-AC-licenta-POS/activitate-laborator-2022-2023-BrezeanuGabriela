package pos.mongPlaylists.Exceptions.JWTExceptions;

// pica testul de integritate local
public class CorruptedJwt extends RuntimeException{
    public CorruptedJwt(String message)
    {
        super(message);
    }
}
