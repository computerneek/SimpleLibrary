package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
public class PacketEncryptionNotSupported extends PacketString{
    private static PacketEncryptionNotSupported baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketEncryptionNotSupported.class.getName());
    public PacketEncryptionNotSupported(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketEncryptionNotSupported(String value){
        super(value);
    }
    @Override
    public Packet newInstance(){
        return new PacketEncryptionNotSupported();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
}
