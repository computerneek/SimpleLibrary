package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigLong extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigLong.class.getName());
    private long data;
    ConfigLong(String key, long value){
        setName(key);
        data = value;
    }
    ConfigLong(){}
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        data = in.readLong();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        out.writeLong(data);
    }
    @Override
    Long getData(){
        return data;
    }
}
