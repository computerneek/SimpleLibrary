package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigBoolean extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigBoolean.class.getName());
    private boolean data;
    ConfigBoolean(boolean value){
        data = value;
    }
    ConfigBoolean(){}
    @Override
    void read(DataInputStream in, short version) throws IOException{
        //Version 0:  Read/write key.  Handled outside, ignore.
        //Version 1:  Current
        data = in.readBoolean();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeBoolean(data);
    }
    @Override
    Boolean getData(){
        return data;
    }
}
