package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigFloat extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigFloat.class.getName());
    private float data;
    ConfigFloat(float value){
        data = value;
    }
    ConfigFloat(){}
    @Override
    void read(DataInputStream in, short version) throws IOException{
        //Version 0:  Read/write key.  Handled outside, ignore.
        //Version 1:  Current
        data = in.readFloat();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeFloat(data);
    }
    @Override
    Float getData(){
        return data;
    }
}
