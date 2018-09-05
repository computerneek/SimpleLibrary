package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import simplelibrary.config2.Config;
import simplelibrary.encryption.Encryption;
import simplelibrary.encryption.EncryptionOutputStream;
import simplelibrary.net.authentication.Authentication;
public class PacketAuthenticated implements Packet {
    private static PacketAuthenticated baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketAuthenticated.class.getName());
    private Authentication auth;
    public PacketAuthenticated(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public Authentication getAuth(){
        return auth;
    }
    public PacketAuthenticated(Authentication auth){
        this.auth = auth;
    }
    @Override
    public Packet newInstance(){
        return new PacketAuthenticated();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        auth = Authentication.read(new DataInputStream(Encryption.UNENCRYPTED.decrypt(in)));
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        out = Encryption.UNENCRYPTED.encrypt(out);
        auth.write(out);
        ((EncryptionOutputStream)out).flush();
    }
    @Override
    public String toString(){
        return getClass().getName()+"(auth="+auth+")";
    }
}
