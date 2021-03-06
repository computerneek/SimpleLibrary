package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.logging.Logger;
public class PacketAuthenticationRequired implements Packet {
    private static PacketAuthenticationRequired baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketAuthenticationRequired.class.getName());
    public PacketAuthenticationRequired(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    @Override
    public Packet newInstance(){
        return new PacketAuthenticationRequired();
    }
    @Override
    public void readPacketData(DataInputStream datastream){}
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream data){}
    @Override
    public String toString(){
        return getClass().getName()+"()";
    }
}
