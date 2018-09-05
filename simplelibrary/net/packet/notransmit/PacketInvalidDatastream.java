package simplelibrary.net.packet.notransmit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import simplelibrary.net.packet.Packet;
/**
 * A class used by the connection subsystem to report an invalid inbound datastream.  Some data WILL be lost during the recovery.
 * @author Bryan
 */
public class PacketInvalidDatastream implements Packet{
    private static final Logger LOG = Logger.getLogger(PacketInvalidDatastream.class.getName());
    @Override
    public Packet newInstance(){
        throw new IllegalStateException("This packet type not to be transmitted.");
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        throw new IllegalStateException("This packet type not to be transmitted.");
    }
    @Override
    public Packet baseInstance(){
        throw new IllegalStateException("This packet type not to be transmitted.");
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        throw new IllegalStateException("This packet type not to be transmitted.");
    }
    @Override
    public String toString(){
        return getClass().getName();
    }
}
