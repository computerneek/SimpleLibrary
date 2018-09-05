package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import simplelibrary.numbers.HugeLong;
class ConfigHugeLong extends ConfigBase{
    private static final HugeLong baseInstance;
    private static final Field field;
    private HugeLong data;
    static {
        try{
            Constructor<HugeLong> constructor = HugeLong.class.getDeclaredConstructor(new Class<?>[0]);
            field = HugeLong.class.getDeclaredField("digits");
            constructor.setAccessible(true);
            field.setAccessible(true);
            baseInstance = constructor.newInstance(new Object[0]);
        }catch(NoSuchMethodException|SecurityException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException|NoSuchFieldException ex){
            throw new RuntimeException(ex);
        }
    }
    ConfigHugeLong(String key, HugeLong value){
        setName(key);
        data = value;
    }
    ConfigHugeLong(){}
    @Override
    void read(DataInputStream in) throws IOException{
        setName(in.readUTF());
        data = baseInstance.copy();
        boolean[] da;
        try{
            da = (boolean[])field.get(data);
        }catch(IllegalArgumentException|IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
        for(int i = 0; i<64; i++){
            long dat = in.readLong();
            for(int j = 0; j<64; j++){
                da[i*64+63-j] = dat%2==1;
                dat>>=1;
            }
        }
    }
    @Override
    void write(DataOutputStream out) throws IOException{
        out.writeUTF(getName());
        boolean[] da;
        try{
            da = (boolean[])field.get(data);
        }catch(IllegalArgumentException|IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
        for(int i = 0; i<64; i++){
            long dat = 0;
            for(int j = 0; j<64; j++){
                if(da[i*64+j]){
                    dat++;
                }
                dat<<=1;
            }
            out.writeLong(dat);
        }
    }
    @Override
    HugeLong getData(){
        return data;
    }
}
