package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigDouble extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigDouble.class.getName());
    private double data;
    ConfigDouble(double value){
        data = value;
    }
    ConfigDouble(){}
    @Override
    void read(DataInputStream in, short version) throws IOException{
        //Version 0:  Read/write key.  Handled outside, ignore.
        //Version 1:  Current
        data = in.readDouble();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeDouble(data);
    }
    @Override
    Double getData(){
        return data;
    }
}
