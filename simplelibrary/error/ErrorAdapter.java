package simplelibrary.error;
import java.util.logging.Level;
import java.util.logging.Logger;
public abstract class ErrorAdapter implements ErrorHandler{
    private static final Logger LOG = Logger.getLogger(ErrorAdapter.class.getName());
    @Override
    public void log(String message, Throwable error, ErrorCategory category){
        System.err.println("Log in catagory "+category.name());
        LOG.log(Level.SEVERE, message, error);
    }
    @Override
    public void warningError(String message, Throwable error, ErrorCategory category){
        System.err.println("Warning in catagory "+category.name());
        LOG.log(Level.SEVERE, message, error);
    }
    @Override
    public void minorError(String message, Throwable error, ErrorCategory category){
        System.err.println("Minor error in catagory "+category.name());
        LOG.log(Level.SEVERE, message, error);
    }
    @Override
    public void moderateError(String message, Throwable error, ErrorCategory category){
        System.err.println("Moderate error in catagory "+category.name());
        LOG.log(Level.SEVERE, message, error);
    }
    @Override
    public void severeError(String message, Throwable error, ErrorCategory category){
        System.err.println("Severe error in catagory "+category.name());
        LOG.log(Level.SEVERE, message, error);
    }
    @Override
    public void criticalError(String message, Throwable error, ErrorCategory category){
        System.err.println("Critical error in catagory "+category.name());
        LOG.log(Level.SEVERE, message, error);
    }
}
