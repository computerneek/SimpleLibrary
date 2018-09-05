package simplelibrary.net.packet.notransmit;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import simplelibrary.encryption.Encryption;
import simplelibrary.net.ConnectionManager;
import simplelibrary.net.packet.Packet;
/**
 * A class used by the connection subsystem to report file receipt progress.  Do not transmit.
 * @author Bryan
 */
public class PacketEncryptionRequired implements Packet{
    private static final Logger LOG = Logger.getLogger(PacketEncryptionRequired.class.getName());
    private ConnectionManager connection;
    private Encryption.ReadyEncryption encryption;
    public PacketEncryptionRequired(ConnectionManager connection, Encryption.ReadyEncryption re) {
        this.connection = connection;
        this.encryption = re;
    }
    public String getEncryptionTitle(){
        return encryption.name;
    }
    public void accept(){
        connection.forceEncrypt(encryption);
        connection = null;
        encryption = null;
    }
    public void decline() throws IOException{
        connection.inboundPackets.enqueue(new PacketConnectionFailed("Other side required encryption "+encryption.name+", but was denied"));
        encryption = null;
        connection.close();
        connection = null;
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
        return getClass().getName()+"(secure data)";
    }
}
