package pos.mongPlaylists.SoapClient;

import com.example.consumingwebservice.wsdl.Authorize;
import com.example.consumingwebservice.wsdl.AuthorizeResponse;
import com.example.consumingwebservice.wsdl.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class ClientForSoap extends WebServiceGatewaySupport{
    private static final Logger log = LoggerFactory.getLogger(ClientForSoap.class);

    public String AuthorizeUser(String token) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Authorize.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // format the XML output
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ObjectFactory objectFactory = new ObjectFactory();

        Authorize request = new Authorize();
        //token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwOi8vMTI3LjAuMC4xOjgwMDAiLCJzdWIiOjQwLCJleHAiOjE2NzEwNjgzMzUsImp0aSI6ImM5NmMxN2ZiLTZlZDgtNDdkZC1iNTk2LTRmMzdmMDdhZGFkNSJ9.rE7zVBfpaQTRa4CN9oZnSu0Cl2imgKYred560lIg4wk";

        JAXBElement<String> jaxbElement = objectFactory.createAuthorizeToken(token);
        request.setToken(jaxbElement);
        JAXBElement<Authorize> auth = objectFactory.createAuthorize(request);

        JAXBElement<AuthorizeResponse> authResp = (JAXBElement<AuthorizeResponse>) getWebServiceTemplate()
                .marshalSendAndReceive("http://127.0.0.1:8000", auth,
                        null);

        String result = authResp.getValue().getAuthorizeResult().getValue();
        return result;
    }

}
