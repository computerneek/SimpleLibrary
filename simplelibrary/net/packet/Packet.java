package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
public interface Packet {
    Packet newInstance();
    void readPacketData(DataInputStream in) throws IOException;
    Packet baseInstance();
    void writePacketData(DataOutputStream out) throws IOException;
}
