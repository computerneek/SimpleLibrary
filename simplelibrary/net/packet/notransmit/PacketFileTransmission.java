package simplelibrary.net.packet.notransmit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import simplelibrary.net.packet.Packet;
/**
 * A class used by the connection subsystem to report file receipt progress.  Do not transmit.
 * @author Bryan
 */
public class PacketFileTransmission implements Packet{
    private static final Logger LOG = Logger.getLogger(PacketFileTransmission.class.getName());
    public final String filename;
    public final String filepath;
    public final int packetNumber;
    public final int totalPacketCount;
    public PacketFileTransmission(String filename, String filepath, int packetNumber, int totalPacketCount){
        this.filename = filename;
        this.filepath = filepath;
        this.packetNumber = packetNumber;
        this.totalPacketCount = totalPacketCount;
    }
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
        return getClass().getName()+"(filename="+filename+"&filepath="+filepath+"&packetNumber="+packetNumber+"&totalPacketCount="+totalPacketCount+")";
    }
}
