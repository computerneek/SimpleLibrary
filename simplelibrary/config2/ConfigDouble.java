package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigDouble extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigDouble.class.getName());
    private double data;
    ConfigDouble(String key, double value){
        setName(key);
        data = value;
    }
    ConfigDouble(){}
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        data = in.readDouble();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        out.writeDouble(data);
    }
    @Override
    Double getData(){
        return data;
    }
}
