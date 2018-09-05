package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import simplelibrary.config2.Config;
public class PacketConfig implements Packet{
    private static PacketConfig baseInstance;
    private static final Logger LOG = Logger.getLogger(PacketConfig.class.getName());
    public Config value;
    public PacketConfig(){
        if(baseInstance==null){
            baseInstance = this;
        }
    }
    public PacketConfig(Config value){
        this.value = value;
    }
    @Override
    public Packet newInstance(){
        return new PacketConfig();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException{
        value = Config.newConfig().load(in);
    }
    @Override
    public Packet baseInstance(){
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException{
        value.save(out);
    }
    @Override
    public String toString(){
        return getClass().getName()+"(value="+value+")";
    }
}
