package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigBoolean extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigBoolean.class.getName());
    private boolean data;
    ConfigBoolean(String key, boolean value){
        setName(key);
        data = value;
    }
    ConfigBoolean(){}
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        data = in.readBoolean();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        out.writeBoolean(data);
    }
    @Override
    Boolean getData(){
        return data;
    }
}
