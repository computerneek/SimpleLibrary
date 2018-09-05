package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
public class PacketString implements Packet{
    private static PacketString baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketString.class.getName());
    public String value;
    public PacketString(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketString(String value){
        this.value = value;
    }
    @Override
    public Packet newInstance(){
        return new PacketString();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        value = in.readUTF();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        out.writeUTF(value);
    }
    @Override
    public String toString(){
        return getClass().getName()+"(value="+value+")";
    }
}
