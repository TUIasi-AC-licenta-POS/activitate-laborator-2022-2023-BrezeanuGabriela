package com.example.spotify.JWT;

import com.example.spotify.Exceptions.JWTExceptions.CorruptedJwt;
import com.example.spotify.Exceptions.JWTExceptions.ExpiredJwt;
import com.example.spotify.Exceptions.JWTExceptions.InvalidJwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.stereotype.Component;
import java.io.Serializable;


@Component
public class JwtTokenUtil implements Serializable {
    private Integer NO_MIN = 60;
    private Integer NO_SECONDS = 60;
    private Integer MILIS = 1000;
    private String secret= "secret";
    private String ISSUER = "http://127.0.0.1:8000";

    //for retrieveing any information from token we will need the secret key
    public Claims getAllClaimsFromToken(String token) {
        try
        {
            return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(token).getBody();
        }
        // pica testul de integritate
        catch (SignatureException signatureException)
        {
            throw new CorruptedJwt("Corrupted JWT!");
        }
    }

    public boolean checkIfIsJwt(String token)
    {
        if(!token.contains("Error"))
            return true;
        else
            return false;
    }

    public String extractErrorMessage(String error)
    {
        String[] reason = error.split(":");
        System.out.println(reason[1]);
        return reason[1];
    }

    public void extractErrorCode(String error)
    {
        if(error.contains("Expired"))
            throw new ExpiredJwt();
        else if(error.contains("invalid") || error.contains("black-listat"))
            throw new InvalidJwt();
    }

    public boolean isJwtExpired(Claims claims)
    {
        long current_time_seconds = System.currentTimeMillis()/MILIS;
        long exp = Long.parseLong(claims.get("exp").toString()) - 2L * NO_MIN * NO_SECONDS ;

        return (exp - current_time_seconds) < 0;
    }

    public boolean isIssuerKnown(Claims claims)
    {
        String iss = claims.getIssuer();
        return !iss.equals(ISSUER);
    }
}
