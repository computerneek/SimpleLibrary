package simplelibrary.net.authentication;
import simplelibrary.config2.Config;
import simplelibrary.encryption.Encryption;
public abstract class Authenticator {
    /**
     * Authenticates a client, based on the authentication data provided by the client, may block indefinitely
     * @param authData The authentication data provided by the client
     * @param outbound The encryption applied to outbound data, to the client
     * @param inbound The encryption applied to inbound data, to the server
     * @return The authentication for the client (use <code>null</code> to denote auth failed)
     */
    public abstract Authentication authenticate(Config authData, Encryption outbound, Encryption inbound);
}
