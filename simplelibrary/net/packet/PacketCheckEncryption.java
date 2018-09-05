package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
public class PacketCheckEncryption extends PacketString{
    private static PacketCheckEncryption baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketCheckEncryption.class.getName());
    public PacketCheckEncryption(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketCheckEncryption(String value){
        super(value);
    }
    @Override
    public Packet newInstance(){
        return new PacketCheckEncryption();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
}
