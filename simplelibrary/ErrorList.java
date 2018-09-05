package simplelibrary;
import java.util.ArrayList;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
public class ErrorList {
    private ArrayList<Object[]> list = new ArrayList<>();
    public void add(ErrorLevel level, String message, Throwable error, ErrorCategory catagory, boolean log){
        if(level==null&&error!=null){
            level = ErrorLevel.log;
        }else if(level==null){
            add(ErrorLevel.severe, null, new IllegalArgumentException("Error must have a level or an exception!"), ErrorCategory.bug);
            return;
        }
        if(message==null&&error==null){
            add(ErrorLevel.severe, null, new IllegalArgumentException("A message or exception must be tied to an error!"), ErrorCategory.bug);
            return;
        }
        if(error == null&&(level==ErrorLevel.critical||level==ErrorLevel.severe)){
            error = new UnknownError();
        }
        if(message==null){
            message = "";
        }
        if(level==null||catagory==null){
            String amessage = "";
            if(level==null&&catagory==null){
                amessage = "Error must be assigned a level and a catagory!";
            }else if(level==null){
                amessage = "Error must be assigned a level!";
            }else if(catagory==null){
                amessage = "Error must be assigned a catagory!";
            }
            add(ErrorLevel.severe, null, new IllegalArgumentException(amessage), ErrorCategory.bug);
            return;
        }
        list.add(new Object[]{level, message, error, catagory, log});
    }
    public void add(ErrorLevel level, String message, Throwable error, ErrorCategory catagory){
        list.add(new Object[]{level, message, error, catagory});
    }
    public void throwAll(){
        for(Object[] obj : list){
            ErrorLevel level = (ErrorLevel)obj[0];
            String message = (String)obj[1];
            Throwable error = (Throwable)obj[2];
            ErrorCategory category = (ErrorCategory)obj[3];
            boolean log = obj.length>4?(boolean)obj[4]:Sys.log;
            if(log){
                Sys.log(message, error);
            }
            level.error(Sys.handler, message, error, category);
        }
        list.clear();
    }
    public int getCount(){
        return list.size();
    }
}
