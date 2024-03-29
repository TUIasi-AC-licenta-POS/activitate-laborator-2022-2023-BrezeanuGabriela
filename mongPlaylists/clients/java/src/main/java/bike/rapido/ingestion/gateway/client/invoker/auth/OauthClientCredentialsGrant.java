package bike.rapido.ingestion.gateway.client.invoker.auth;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2022-12-28T01:49:35.171524+02:00[Europe/Athens]")
public class OauthClientCredentialsGrant extends OAuth {

  public OauthClientCredentialsGrant(String authorizationUrl, String tokenUrl, String scopes) {
    super(authorizationUrl, tokenUrl, scopes);
  }

  @Override
  protected OAuth2AccessToken getOAuth2AccessToken() {
    try {
      return service.getAccessTokenClientCredentialsGrant(scopes);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get oauth token", e);
    }
  }

  @Override
  protected OAuthFlow getFlow() {
    return OAuthFlow.application;
  }

  /**
   * Configures the client credentials flow
   *
   * @param clientId
   * @param clientSecret
   */
  public void configure(String clientId, String clientSecret) {
    service = new ServiceBuilder(clientId)
            .apiSecret(clientSecret)
            .defaultScope(scopes)
            .build(new DefaultApi20Impl(authorizationUrl, tokenUrl));
  }
}
