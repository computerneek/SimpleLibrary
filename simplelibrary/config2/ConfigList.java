package simplelibrary.config2;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import simplelibrary.numbers.HugeLong;
public class ConfigList extends ConfigBase{
    private ArrayList<ConfigBase> lst = new ArrayList<>();
    public ConfigList(){}
    public ConfigList(Collection<?> lst){
        for(Object o : lst){
            add(o);
        }
    }
    @Override
    void read(DataInputStream in) throws IOException {
        setName(in.readUTF());
        int index;
        ConfigBase base;
        while((index = in.read())>0){
            base = newConfig(index);
            base.read(in);
            lst.add(base);
        }
    }
    @Override
    void write(DataOutputStream out) throws IOException {
        out.writeUTF(getName());
        for(ConfigBase base : lst){
            out.write(base.getIndex());
            base.write(out);
        }
        out.write(0);
    }
    @Override
    ConfigList getData() {
        return this;
    }
    public <V> V get(int index){
        if(index>=size()||index<0) return null;
        else return (V)lst.get(index).getData();
    }
    public <V> Collection<V> copyTo(Collection<V> lst){
        for(V v : this.<V>iterable()){
            lst.add(v);
        }
        return lst;
    }
    public <V> Iterable<V> iterable(){
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return ConfigList.this.iterator();
            }
        };
    }
    public <V> Iterator<V> iterator(){
        return new Iterator<V>(){
            Iterator<ConfigBase> it = lst.iterator();
            V next;
            @Override
            public boolean hasNext() {
                while(next==null&&it.hasNext()){
                    try{
                        next = (V)it.next().getData();
                    }catch(ClassCastException ex){}
                }
                return next!=null;
            }
            @Override
            public V next() {
                hasNext();
                V val = next;
                next = null;
                return val;
            }
        };
    }
    public int size(){
        return lst.size();
    }
    public <V> V remove(int index){
        V val = get(index);
        if(index>=0&&index<size()) lst.remove(index);
        return val;
    }
    public void add(Config value){
        doAdd(value);
    }
    public void add(String value){
        doAdd(new ConfigString("", value));
    }
    public void add(int value){
        doAdd(new ConfigInteger("", value));
    }
    public void add(boolean value){
        doAdd(new ConfigBoolean("", value));
    }
    public void add(float value){
        doAdd(new ConfigFloat("", value));
    }
    public void add(long value){
        doAdd(new ConfigLong("", value));
    }
    public void add(double value){
        doAdd(new ConfigDouble("", value));
    }
    public void add(HugeLong value){
        doAdd(new ConfigHugeLong("", value));
    }
    public void add(ConfigList value){
        doAdd(value);
    }
    public void add(Object value){
        if(value==null){
        }else if(value instanceof Config){
            add((Config)value);
        }else if(value instanceof String){
            add((String)value);
        }else if(value instanceof Integer){
            add((int)value);
        }else if(value instanceof Boolean){
            add((boolean)value);
        }else if(value instanceof Float){
            add((float)value);
        }else if(value instanceof Long){
            add((long)value);
        }else if(value instanceof Double){
            add((double)value);
        }else if(value instanceof HugeLong){
            add((HugeLong)value);
        }else if(value instanceof ConfigList){
            add((ConfigList)value);
        }
    }
    private void doAdd(ConfigBase b){
        if(b==null) throw new IllegalArgumentException("Cannot set null values to a config!");
        lst.add(b);
    }
}
