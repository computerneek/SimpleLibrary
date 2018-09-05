package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
public class PacketEncryptionSupported extends PacketString{
    private static PacketEncryptionSupported baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketEncryptionSupported.class.getName());
    public PacketEncryptionSupported(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketEncryptionSupported(String value){
        super(value);
    }
    @Override
    public Packet newInstance(){
        return new PacketEncryptionSupported();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
}
