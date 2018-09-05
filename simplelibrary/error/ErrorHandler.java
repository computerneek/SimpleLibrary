package simplelibrary.error;
public interface ErrorHandler{
    void log(String message, Throwable error, ErrorCategory category);
    void warningError(String message, Throwable error, ErrorCategory category);
    void minorError(String message, Throwable error, ErrorCategory category);
    void moderateError(String message, Throwable error, ErrorCategory category);
    void severeError(String message, Throwable error, ErrorCategory category);
    void criticalError(String message, Throwable error, ErrorCategory category);
}
