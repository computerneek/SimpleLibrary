package simplelibrary;
import java.util.ArrayList;
import java.util.Iterator;
public class Stack<T> implements Iterable<T>{
    private StackEntry top;
    private int size;
    public void push(T obj){
        StackEntry t = new StackEntry(obj);
        if(top==null){
            top = t;
            size = 1;
        }else{
            t.next = top;
            top = t;
            size++;
        }
    }
    public T pop(){
        if(top==null){
            size = 0;
            return null;
        }
        T obj = top.obj;
        top = top.next;
        size--;
        return obj;
    }
    public T peek(){
        if(top==null){
            return null;
        }
        return top.obj;
    }
    public void clear(){
        top = null;
        size = 0;
    }
    public int size(){
        return recountSize();
    }
    public boolean isEmpty(){
        return top==null;
    }
    /**
     * Clones this Stack into a like-typed ArrayList, leaving the Stack untouched.
     */
    public ArrayList<T> toList(){
        ArrayList<T> t = new ArrayList<>(size);
        for(T i : this) t.add(i);
        return t;
    }
    public Stack<T> copy() {
        Stack<T> q = new Stack<T>();
        if(top==null) return q;
        StackEntry h = top.next;
        StackEntry h2 = new StackEntry(top.obj);
        q.top = h2;
        while(h!=null){
            h2.next = new StackEntry(h.obj);
            h2 = h2.next;
            h = h.next;
        }
        return q;
    }
    public int recountSize() {
        StackEntry h = top;
        size = 0;
        while(h!=null){
            size++;
            h = h.next;
        };
        return size;
    }
    public boolean contains(T obj){
        for(T t : this){
            if((t==null&&obj==null)||(obj!=null&&obj.equals(t))) return true;
        }
        return false;
    }
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            StackEntry next = top;
            @Override
            public boolean hasNext() {
                return next!=null;
            }
            @Override
            public T next() {
                T obj = next.obj;
                next = next.next;
                return obj;
            }
        };
    }
    protected class StackEntry{
        protected T obj;
        protected StackEntry next;
        private StackEntry(T obj){
            this.obj = obj;
        }
    }
}
