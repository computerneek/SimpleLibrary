package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
abstract class ConfigBase{
    private String name = "";
    protected ConfigBase(){}
    protected ConfigBase newConfig(int index){
        switch(index){
            case 1:
                return new Config();
            case 2:
                return new ConfigString();
            case 3:
                return new ConfigInteger();
            case 4:
                return new ConfigFloat();
            case 5:
                return new ConfigBoolean();
            case 6:
                return new ConfigLong();
            case 7:
                return new ConfigDouble();
            case 8:
                return new ConfigHugeLong();
            case 9:
                return new ConfigList();
            default:
                throw new AssertionError(index);
        }
    }
    abstract void read(DataInputStream in) throws IOException;
    abstract void write(DataOutputStream out) throws IOException;
    void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    int getIndex(){
        Class<? extends ConfigBase> clazz = getClass();
        if(clazz==Config.class){
            return 1;
        }else if(clazz==ConfigString.class){
            return 2;
        }else if(clazz==ConfigInteger.class){
            return 3;
        }else if(clazz==ConfigFloat.class){
            return 4;
        }else if(clazz==ConfigBoolean.class){
            return 5;
        }else if(clazz==ConfigLong.class){
            return 6;
        }else if(clazz==ConfigDouble.class){
            return 7;
        }else if(clazz==ConfigHugeLong.class){
            return 8;
        }else if(clazz==ConfigList.class){
            return 9;
        }else{
            throw new AssertionError(clazz.getName());
        }
    }
    abstract Object getData();
}
