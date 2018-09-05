package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import simplelibrary.config2.Config;
import simplelibrary.net.authentication.Authenticator;
import simplelibrary.encryption.Encryption;
import simplelibrary.encryption.EncryptionOutputStream;
public class PacketAuthentication extends PacketConfig {
    private static PacketAuthentication baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketAuthentication.class.getName());
    private Config data;
    public PacketAuthentication(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketAuthentication(String username, String password){
        this(byUsernameAndPassword(username, password));
    }
    public PacketAuthentication(Config authData){
        super(authData);
    }
    private static Config byUsernameAndPassword(String username, String password){
        Config c = Config.newConfig();
        c.set("username", username+"");//Prevent null pointers if given no credentials
        c.set("password", password+"");
        return c;
    }
    @Override
    public Packet newInstance(){
        return new PacketAuthentication();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        value = Config.newConfig().load(Encryption.UNENCRYPTED.decrypt(in));
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        out = Encryption.UNENCRYPTED.encrypt(out);
        value.save(out);
        ((EncryptionOutputStream)out).flush();
    }
    @Override
    public String toString(){
        return getClass().getName()+"(value="+value+")";
    }
}
