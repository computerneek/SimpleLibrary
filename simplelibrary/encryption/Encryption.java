package simplelibrary.encryption;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
public abstract class Encryption {
    private static final HashMap<String, Encryption> encryptions = new HashMap<>();
    public static final ReadyEncryption UNENCRYPTED = new NothingEncryption();
    public final String name;
    public static boolean isSupported(String encryption) {
        String[] components = encryption.split(" + ");
        for(String s : components){
            if(!encryptions.containsKey(s)){
                return false;
            }
        }
        return true;
    }
    public Encryption(String name){
        this.name = name;
        if(!(this instanceof ReadyEncryption)||(this instanceof NothingEncryption)){
            if(encryptions.containsKey(name)){
                throw new IllegalArgumentException("Encryption already exists by that name!");
            }else if(name.contains(" + ")&&!(this instanceof LayeredEncryption)){
                throw new IllegalArgumentException("Single encryptions cannot have ' + ' in their names!");
            }
            encryptions.put(this.name, this);
        }
    }
    public static Encryption getEncryption(String encryption) throws EncryptionNotFoundException{
        String[] components = encryption.split(" + ");
        for(String s : components){
            if(!encryptions.containsKey(s)){
                throw new EncryptionNotFoundException(s);
            }
        }
        if(components.length==1){
            return encryptions.get(components[0]);
        }else{
            ArrayList<String> encryptions = new ArrayList<>(Arrays.asList(components));
            for(int i = encryptions.size(); i>1; i--){
                for(int j = 0; j<=encryptions.size()-i; j++){
                    String title = encryptions.get(j);
                    for(int k = 1; k<i; k++){
                        title+=" + "+encryptions.get(j+k);
                    }
                    if(Encryption.encryptions.containsKey(title)){
                        for(int k = i-1; k>0; k++){
                            encryptions.remove(k+j);
                        }
                        encryptions.set(j, title);
                    }
                }
            }
            if(encryptions.size()==1){
                return Encryption.encryptions.get(encryptions.get(0));
            }else{
                Encryption e = Encryption.encryptions.get(encryptions.get(0));
                for(int i = 1; i<encryptions.size(); i++){
                    e = new LayeredEncryption(e, Encryption.encryptions.get(encryptions.get(0)));
                }
                return e;
            }
        }
    }
    public final ReadyEncryption ready(String key){
        if(this instanceof NothingEncryption){
            return (ReadyEncryption)this;
        }
        if(this instanceof LayeredEncryption){
            return new ReadyLayeredEncryption((LayeredEncryption)this, key);
        }else{
            return new ReadyEncryption(this, key);
        }
    }
    protected abstract EncryptionOutputStream encrypt(OutputStream out, String key);
    protected abstract DecryptionInputStream decrypt(InputStream in, String key);
    public static class LayeredEncryption extends Encryption{
        private final Encryption firstLayer;
        private final Encryption secondLayer;
        private LayeredEncryption(Encryption innerLayer, Encryption outerLayer){
            super(innerLayer.name+" + "+outerLayer.name);
            this.firstLayer = innerLayer;
            this.secondLayer = outerLayer;
        }
        @Override
        protected EncryptionOutputStream encrypt(OutputStream out, String key) {
            return secondLayer.encrypt(firstLayer.encrypt(out, key), key);
        }
        @Override
        protected DecryptionInputStream decrypt(InputStream in, String key) {
            return firstLayer.decrypt(secondLayer.decrypt(in, key), key);
        }
        public final ReadyEncryption readyLayers(String... key){
            return new ReadyLayeredEncryption(this, key);
        }
    }
    public static class ReadyEncryption extends Encryption{
        private String key;
        private final Encryption encryption;
        private ReadyEncryption(String name){
            super(name);
            this.encryption = null;
        }
        private ReadyEncryption(Encryption encryption){
            super(encryption.name);
            this.encryption = encryption;
        }
        private ReadyEncryption(Encryption encryption, String key) {
            this(encryption);
            this.key = key;
        }
        @Override
        protected EncryptionOutputStream encrypt(OutputStream out, String key) {
            return encryption.encrypt(out, key);
        }
        @Override
        protected DecryptionInputStream decrypt(InputStream in, String key) {
            return encryption.decrypt(in, key);
        }
        public EncryptionOutputStream encrypt(OutputStream out){
            return encryption.encrypt(out, key);
        }
        public DecryptionInputStream decrypt(InputStream in){
            return encryption.decrypt(in, key);
        }
        @Override
        public boolean equals(Object obj) {
            return obj!=null&&obj.getClass()==getClass()&&key.equals(((ReadyEncryption)obj).key);
        }
    }
    public static class ReadyLayeredEncryption extends ReadyEncryption{
        ArrayList<ReadyEncryption> encryptions;
        private ReadyLayeredEncryption(LayeredEncryption encryption, String... keys){
            super(encryption);
            int index = 0;
            ArrayList<Encryption> lst = new ArrayList<>();
            lst.add(encryption);
            while(!lst.isEmpty()){
                Encryption e = lst.remove(0);
                if(!(e instanceof LayeredEncryption)){
                    encryptions.add(e.ready(keys[index]));
                    index = (index+1)%keys.length;
                }else{
                    LayeredEncryption le = (LayeredEncryption) e;
                    lst.add(0, le.secondLayer);
                    lst.add(0, le.firstLayer);
                }
            }
        }
        public EncryptionOutputStream encrypt(OutputStream out){
            for (ReadyEncryption encryption : encryptions) {
                out = encryption.encrypt(out);
            }
            return (EncryptionOutputStream)out;
        }
        public DecryptionInputStream decrypt(InputStream in){
            for(ReadyEncryption encryption : encryptions){
                in = encryption.decrypt(in);
            }
            return (DecryptionInputStream)in;
        }
        @Override
        public boolean equals(Object obj) {
            if(obj==null||obj.getClass()!=getClass()){
                return false;
            }
            ReadyLayeredEncryption o = (ReadyLayeredEncryption) obj;
            if(o.encryptions.size()!=encryptions.size()){
                return false;
            }
            for(int i = 0; i<encryptions.size(); i++){
                if(!encryptions.get(i).equals(o.encryptions.get(i))){
                    return false;
                }
            }
            return true;
        }
    }
    public static class NothingEncryption extends ReadyEncryption{
        public NothingEncryption() {
            super("");
        }
        @Override
        protected EncryptionOutputStream encrypt(OutputStream out, String key) {
            return new EncryptionOutputStream(out) {
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }
                @Override
                public void flush() throws IOException {
                    out.flush();
                }
            };
        }
        protected DecryptionInputStream decrypt(InputStream in, String key){
            return new DecryptionInputStream(in) {
                @Override
                public int read() throws IOException {
                    return in.read();
                }
            };
        }
        public EncryptionOutputStream encrypt(OutputStream out){
            return encrypt(out, null);
        }
        public DecryptionInputStream decrypt(InputStream in){
            return decrypt(in, null);
        }
    }
}
