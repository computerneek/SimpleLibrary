package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
public class PacketLong implements Packet{
    private static PacketLong baseInstance;
    public long value;
    public PacketLong(long value){
        this.value = value;
    }
    public PacketLong(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    @Override
    public Packet newInstance(){
        return new PacketLong();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        value = in.readLong();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        out.writeLong(value);
    }
    @Override
    public String toString(){
        return getClass().getName()+"(value="+value+")";
    }
}
