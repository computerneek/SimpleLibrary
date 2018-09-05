package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.logging.Logger;
/**
 * Used internally by the ConnectionManager subsystem to report an invalid datastream to the sending side.
 * THIS PACKET WILL BE LEFT IN INBOUND PACKET STREAM ON THE SENDING SIDE.
 * May be ignored, datastream recovery is automatic.  HOWEVER, some OUTBOUND data will have been lost.
 * @author Bryan
 */
public class PacketValidateDatastream implements Packet {
    private static PacketValidateDatastream baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketValidateDatastream.class.getName());
    public PacketValidateDatastream(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    @Override
    public Packet newInstance(){
        return new PacketValidateDatastream();
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
