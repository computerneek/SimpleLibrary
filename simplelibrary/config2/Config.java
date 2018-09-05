package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.numbers.HugeLong;
public class Config extends ConfigBase implements Cloneable{
    private static final int TYPE_ALONE = 1;
    private static final int TYPE_FILE = 2;
    private static final int TYPE_STRING = 3;
    private static final int TYPE_STREAM = 4;
    private File file;
    private int type = TYPE_ALONE;
    private String path;
    private InputStream stream;
    private final ArrayList<String> keys = new ArrayList<>();
    private final HashMap<String, ConfigBase> data = new HashMap<>();
    public static Config newConfig(File file){
        Config config = new Config();
        config.type = TYPE_FILE;
        config.file = file;
        return config;
    }
    public static Config newConfig(String path){
        Config config = new Config();
        config.type = TYPE_STRING;
        config.path = path;
        return config;
    }
    public static Config newConfig(InputStream in){
        Config config = new Config();
        config.type = TYPE_STREAM;
        config.stream = in;
        return config;
    }
    public static Config newConfig(){
        return new Config();
    }
    public Config(){}
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        int index;
        ConfigBase base;
        while((index = in.read())>0){
            base = newConfig(index);
            base.read(in);
            dataput(base.getName(), base);
        }
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        ConfigBase base;
        for(String str : keys){
            base = data.get(str);
            out.write(base.getIndex());
            base.write(out);
        }
        out.write(0);
    }
    @Override
    Config getData(){
        return this;
    }
    public Config load(){
        switch(type){
            case TYPE_FILE:
                return load(file);
            case TYPE_STRING:
                return load(path);
            case TYPE_ALONE:
                throw new UnsupportedOperationException("Cannot load a config with nothing to load from!");
            case TYPE_STREAM:
                return load(stream);
            default:
                throw new AssertionError(type);
        }
    }
    public boolean save(){
        switch(type){
            case TYPE_FILE:
                return save(file);
            case TYPE_STRING:
                return save(path);
            case TYPE_ALONE:
                throw new UnsupportedOperationException("Cannot save a config with nothing to save to!");
            case TYPE_STREAM:
                throw new UnsupportedOperationException("Cannot save a config with nothing to save to!");
            default:
                throw new AssertionError(type);
        }
    }
    public Config load(File file){
        try{
            Config value;
            try (FileInputStream in=new FileInputStream(file)) {
                value=load(in);
            }
            return value;
        }catch(Throwable ex){
            return null;
        }
    }
    public boolean save(File file){
        try{
            file.getParentFile().mkdirs();
            if(!file.getParentFile().exists()||!file.getParentFile().isDirectory()||file.isDirectory()){
                return false;
            }
            boolean value;
            try (FileOutputStream out=new FileOutputStream(file)) {
                value=save(out);
            }
            return value;
        }catch(Throwable ex){
            return false;
        }
    }
    public  Config load(String path){
        if(path.startsWith("/")){
            InputStream in = Config.class.getResourceAsStream(path);
            Config value = load(in);
            try{
                in.close();
            }catch(Throwable ex){
                Sys.error(ErrorLevel.severe, "Could not close input stream!", ex, ErrorCategory.fileIO);
            }
            return value;
        }else{
            return load(new File(path));
        }
    }
    public  boolean save(String path){
        if(path.startsWith("/")){
            try{
                URL resource = Config.class.getResource(path);
                if(resource.getQuery()!=null){
                    return false;
                }
                File file = new File(resource.getFile());
                file.getParentFile().mkdirs();
                if(!file.getParentFile().exists()||!file.getParentFile().isDirectory()||file.isDirectory()){
                    return false;
                }
                boolean flag;
                try (FileOutputStream out=new FileOutputStream(file)) {
                    flag=save(out);
                }
                return flag;
            }catch(Throwable twbl){
                return false;
            }
        }else{
            return save(new File(path));
        }
    }
    public Config load(InputStream in){
        if(in==null){
            throw new IllegalArgumentException("Input stream cannot be null!");
        }
        data.clear();
        keys.clear();
        DataInputStream dataIn = new DataInputStream(in);
        try{
            read(dataIn);
        }catch(Throwable ex){
            Sys.error(ErrorLevel.moderate, "Could not load config!", ex, ErrorCategory.config);
            return null;
        }
        return this;
    }
    public  boolean save(OutputStream out){
        if(out==null){
            throw new IllegalArgumentException("Output stream cannot be null!");
        }
        DataOutputStream dataOut = new DataOutputStream(out);
        try{
            write(dataOut);
        }catch(Throwable ex){
            Sys.error(ErrorLevel.moderate, "Could not save config!", ex, ErrorCategory.config);
            return false;
        }
        return true;
    }
    public <V> V get(String key){
        if(!data.containsKey(key)){
            return null;
        }else{
            return (V)data.get(key).getData();
        }
    }
    public <V> V get(String key, V defaultValue){
        if(!data.containsKey(key)){
            set(key, defaultValue);
            return defaultValue;
        }else{
            return (V)data.get(key).getData();
        }
    }
    public String[] properties(){
        return keys.toArray(new String[keys.size()]);
    }
    public <V> V removeProperty(String key){
        V val = get(key);
        data.remove(key);
        keys.remove(key);
        return val;
    }
    public boolean hasProperty(String key){
        return data.containsKey(key);
    }
    public void setProperty(String key, Config value){
        value.setName(key);
        dataput(key, value);
    }
    public void setProperty(String key, String value){
        dataput(key, new ConfigString(key, value));
    }
    public void setProperty(String key, int value){
        dataput(key, new ConfigInteger(key, value));
    }
    public void setProperty(String key, boolean value){
        dataput(key, new ConfigBoolean(key, value));
    }
    public void setProperty(String key, float value){
        dataput(key, new ConfigFloat(key, value));
    }
    public void setProperty(String key, long value){
        dataput(key, new ConfigLong(key, value));
    }
    public void setProperty(String key, double value){
        dataput(key, new ConfigDouble(key, value));
    }
    public void setProperty(String key, HugeLong value){
        dataput(key, new ConfigHugeLong(key, value));
    }
    public void setProperty(String key, ConfigList value){
        value.setName(key);
        dataput(key, value);
    }
    public void set(String key, Config value){
        value.setName(key);
        dataput(key, value);
    }
    public void set(String key, String value){
        dataput(key, new ConfigString(key, value));
    }
    public void set(String key, int value){
        dataput(key, new ConfigInteger(key, value));
    }
    public void set(String key, boolean value){
        dataput(key, new ConfigBoolean(key, value));
    }
    public void set(String key, float value){
        dataput(key, new ConfigFloat(key, value));
    }
    public void set(String key, long value){
        dataput(key, new ConfigLong(key, value));
    }
    public void set(String key, double value){
        dataput(key, new ConfigDouble(key, value));
    }
    public void set(String key, HugeLong value){
        dataput(key, new ConfigHugeLong(key, value));
    }
    public void set(String key, ConfigList value){
        value.setName(key);
        dataput(key, value);
    }
    public void set(String key, Object value){
        if(value==null){
        }else if(value instanceof Config){
            set(key, (Config)value);
        }else if(value instanceof String){
            set(key, (String)value);
        }else if(value instanceof Integer){
            set(key, (int)value);
        }else if(value instanceof Boolean){
            set(key, (boolean)value);
        }else if(value instanceof Float){
            set(key, (float)value);
        }else if(value instanceof Long){
            set(key, (long)value);
        }else if(value instanceof Double){
            set(key, (double)value);
        }else if(value instanceof HugeLong){
            set(key, (HugeLong)value);
        }else if(value instanceof ConfigList){
            set(key, (ConfigList)value);
        }
    }
    private void dataput(String key, ConfigBase base){
        if(base==null) throw new IllegalArgumentException("Cannot set null values to a config!");
        if(!keys.contains(key)){
            keys.add(key);
        }
        data.put(key, base);
    }
    @Override
    public Config clone(){
        Config c = newConfig();
        c.file = file;
        c.type = type;
        c.path = path;
        c.stream = stream;
        c.keys.addAll(keys);
        for(String s : data.keySet()){
            ConfigBase b = data.get(s);
            if(b instanceof Config){
                c.data.put(s, ((Config)b).clone());
            }else{
                c.data.put(s, b);
            }
        }
        return c;
    }
}
