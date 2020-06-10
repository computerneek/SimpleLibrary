package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigByte extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigByte.class.getName());
    private byte data;
    ConfigByte(byte value){
        data = value;
    }
    ConfigByte(){}
    @Override
    void read(DataInputStream in, short version) throws IOException{
        //Version 0:  Read/write key.  Handled outside, ignore.
        //Version 1:  Current
        data = in.readByte();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeByte(data);
    }
    @Override
    Byte getData(){
        return data;
    }
}
