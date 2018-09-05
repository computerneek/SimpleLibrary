package simplelibrary.net.packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplelibrary.encryption.Encryption;
import simplelibrary.encryption.Encryption.ReadyEncryption;
import simplelibrary.encryption.Encryption.ReadyLayeredEncryption;
public class PacketEncryptionStart implements Packet{
    public final ReadyEncryption encryption;
    private String title;
    private String[] keys;
    private static PacketEncryptionStart baseInstance;
    public PacketEncryptionStart(ReadyEncryption e){
        try {
            encryption = e;
            this.title = e.name;
            Field f = ReadyEncryption.class.getDeclaredField("key");
            f.setAccessible(true);
            if(e instanceof ReadyLayeredEncryption){
                Field f2 = ReadyLayeredEncryption.class.getDeclaredField("encryptions");
                f2.setAccessible(true);
                ReadyLayeredEncryption le = (ReadyLayeredEncryption)e;
                ArrayList<ReadyEncryption> lst = (ArrayList<ReadyEncryption>) f2.get(e);
                keys = new String[lst.size()];
                for(int i = 0; i<keys.length; i++){
                    keys[i] = (String) f.get(lst.get(i));
                }
            }else{
                keys = new String[1];
                keys[0] = (String) f.get(e);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
    public PacketEncryptionStart(){
        if(baseInstance==null){
            baseInstance = this;
        }
        encryption = null;
    }
    @Override
    public Packet newInstance() {
        return new PacketEncryptionStart();
    }
    @Override
    public void readPacketData(DataInputStream in) throws IOException {
        title = in.readUTF();
        in = new DataInputStream(Encryption.UNENCRYPTED.decrypt(in));
        keys = new String[in.readInt()];
        for(int i = 0; i<keys.length; i++){
            keys[i] = in.readUTF();
        }
    }
    @Override
    public Packet baseInstance() {
        return baseInstance;
    }
    @Override
    public void writePacketData(DataOutputStream out) throws IOException {
        out.writeUTF(title);
        out = Encryption.UNENCRYPTED.encrypt(out);
        out.writeInt(keys.length);
        for(int i = 0; i<keys.length; i++){
            out.writeUTF(keys[i]);
        }
        out.flush();
    }
    public String getTitle() {
        return this.title;
    }
    public String[] getKeys() {
        String[] k = new String[keys.length];
        System.arraycopy(keys, 0, k, 0, k.length);
        return k;
    }
}
