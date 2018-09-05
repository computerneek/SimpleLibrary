package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
public class PacketBoolean implements Packet{
    private static PacketBoolean baseInstance;
    public boolean value;
    public PacketBoolean(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketBoolean(boolean value){
        this.value = value;
    }
    @Override
    public Packet newInstance(){
        return new PacketBoolean();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        value = in.readBoolean();
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        out.writeBoolean(value);
    }
    @Override
    public String toString(){
        return getClass().getName()+"(value="+value+")";
    }
}
