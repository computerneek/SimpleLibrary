package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
class ConfigString extends ConfigBase{
    private static final Logger LOG = Logger.getLogger(ConfigString.class.getName());
    private String data;
    ConfigString(){}
    ConfigString(String name, String data){
        setName(name);
        if(data==null){
            throw new NullPointerException("Data can't be null!");
        }
        this.data = data;
    }
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        data = in.readUTF();
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        out.writeUTF(data);
    }
    @Override
    String getData(){
        return data;
    }
}
