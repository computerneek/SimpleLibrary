package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
public class PacketInteger implements Packet{
    private static PacketInteger baseInstance;
    public int value;
    public PacketInteger(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketInteger(int value){
        this.value = value;
    }
    @Override
    public Packet newInstance(){
        return new PacketInteger();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        value = in.readInt();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        out.writeInt(value);
    }
    @Override
    public String toString(){
        return getClass().getName()+"(value="+value+")";
    }
}
