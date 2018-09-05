package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigInteger extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigInteger.class.getName());
    private int data;
    ConfigInteger(String key, int value){
        setName(key);
        data = value;
    }
    ConfigInteger(){}
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        data = in.readInt();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        out.writeInt(data);
    }
    @Override
    Integer getData(){
        return data;
    }
}
