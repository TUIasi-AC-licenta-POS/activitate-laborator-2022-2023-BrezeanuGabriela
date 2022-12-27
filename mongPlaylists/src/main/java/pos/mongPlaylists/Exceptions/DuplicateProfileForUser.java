package pos.mongPlaylists.Exceptions;

public class DuplicateProfileForUser extends RuntimeException{
    public DuplicateProfileForUser(Integer id)
    {
        super(String.format("Profile for userID - %d already exists!", id));
    }
}
