package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
public class PacketPingTest implements Packet{
    private static PacketPingTest baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketPingTest.class.getName());
    long nanos = 0;
    boolean reflected = false;
    public PacketPingTest(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    @Override
    public Packet newInstance(){
        return new PacketPingTest();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        nanos = in.readLong();
        reflected = in.readBoolean();
        if(reflected) nanos = System.nanoTime()-nanos;
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        if(nanos==0){
            out.writeLong(System.nanoTime());
            out.writeBoolean(false);
        }else{
            out.writeLong(nanos);
            out.writeBoolean(true);
        }
    }
    public long getTime(){
        return nanos;
    }
    public boolean isReflected(){
        return reflected;
    }
    @Override
    public String toString(){
        return getClass().getName()+"(nanos="+nanos+",reflected="+reflected+")";
    }
}
