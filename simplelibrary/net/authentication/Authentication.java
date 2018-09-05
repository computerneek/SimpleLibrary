package simplelibrary.net.authentication;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import simplelibrary.config2.Config;
import simplelibrary.encryption.Encryption;
import simplelibrary.encryption.Encryption.LayeredEncryption;
import simplelibrary.encryption.EncryptionNotFoundException;
public class Authentication {
    private final String requiredEncryption;
    private final String[] requiredKeys;
    private final String requestedEncryption;
    private final String[] requestedKeys;
    private final Config authData;
    private Authentication(Encryption requiredEncryption, String[] requiredKeys, Encryption requestedEncryption, String[] requestedKeys, Config authData){
        this.requiredEncryption = requiredEncryption==null?null:requiredEncryption.name;
        this.requiredKeys = requiredKeys;
        this.requestedEncryption = requestedEncryption==null?null:requestedEncryption.name;
        this.requestedKeys = requestedKeys;
        this.authData = authData;
    };
    private Authentication(String requiredEncryption, String[] requiredKeys, String requestedEncryption, String[] requestedKeys, Config authData){
        this.requiredEncryption = requiredEncryption;
        this.requiredKeys = requiredKeys;
        this.requestedEncryption = requestedEncryption;
        this.requestedKeys = requestedKeys;
        this.authData = authData;
    };
    public static Authentication read(DataInputStream in) throws IOException {
        String requiredEncryption = null;
        String[] requiredKeys = null;
        String requestedEncryption = null;
        String[] requestedKeys = null;
        Config authData;
        if(in.read()==1){
            requiredEncryption = in.readUTF();
            requiredKeys = new String[in.readInt()];
            for(int i = 0; i<requiredKeys.length; i++){
                requiredKeys[i] = in.readUTF();
            }
        }
        if(in.read()==1){
            requestedEncryption = in.readUTF();
            requestedKeys = new String[in.readInt()];
            for(int i = 0; i<requestedKeys.length; i++){
                requestedKeys[i] = in.readUTF();
            }
        }
        authData = Config.newConfig().load(in);
        return new Authentication(requiredEncryption, requiredKeys, requestedEncryption, requestedKeys, authData);
    }
    public static Authentication authenticate(Config authData){
        return new Authentication((Encryption)null, null, null, null, authData);
    }
    public static Authentication requireEncrypt(Encryption requiredEncryption, String[] requiredKeys, Config authData){
        return new Authentication(requiredEncryption, requiredKeys, null, null, authData);
    }
    public static Authentication requestEncrypt(Encryption requestedEncryption, String[] requestedKeys, Config authData){
        return new Authentication(null, null, requestedEncryption, requestedKeys, authData);
    }
    public static Authentication authenticate(Encryption requiredEncryption, String[] requiredKeys, Encryption requestedEncryption, String[] requestedKeys, Config authData){
        return new Authentication(requiredEncryption, requiredKeys, requestedEncryption, requestedKeys, authData);
    }
    public void write(DataOutputStream out) throws IOException {
        if(requiredEncryption==null){
            out.write(0);
        }else{
            out.write(1);
            out.writeUTF(requiredEncryption);
            out.writeInt(requiredKeys.length);
            for(String s : requiredKeys){
                out.writeUTF(s);
            }
        }
        if(requestedEncryption==null){
            out.write(0);
        }else{
            out.write(1);
            out.writeUTF(requestedEncryption);
            out.writeInt(requestedKeys.length);
            for(String s : requestedKeys){
                out.writeUTF(s);
            }
        }
        authData.save(out);
    }
    public boolean requiresEncryption() {
        return requiredEncryption!=null;
    }
    public String getRequiredEncryption() {
        return requiredEncryption;
    }
    public String getRequestedEncryption() {
        return requestedEncryption;
    }
    public boolean requestsEncryption() {
        return requestedEncryption!=null;
    }
    public Config getAuthData(){
        return authData.clone();
    }
    public Encryption.ReadyEncryption getRequiredReadyEncryption() throws EncryptionNotFoundException {
        Encryption e = Encryption.getEncryption(requiredEncryption);
        if(e instanceof LayeredEncryption){
            return ((LayeredEncryption)e).readyLayers(requiredKeys);
        }else{
            return e.ready(requiredKeys[0]);
        }
    }
    public Encryption.ReadyEncryption getRequestedReadyEncryption() throws EncryptionNotFoundException {
        Encryption e = Encryption.getEncryption(requestedEncryption);
        if(e instanceof LayeredEncryption){
            return ((LayeredEncryption)e).readyLayers(requestedKeys);
        }else{
            return e.ready(requestedKeys[0]);
        }
    }
    
}
