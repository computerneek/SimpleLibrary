package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigFloat extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigFloat.class.getName());
    private float data;
    ConfigFloat(String key, float value){
        setName(key);
        data = value;
    }
    ConfigFloat(){}
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        data = in.readFloat();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        out.writeFloat(data);
    }
    @Override
    Float getData(){
        return data;
    }
}
