package simplelibrary.error;
public abstract class ErrorLevel{
    public static ErrorLevel critical = new ErrorLevel(){
        @Override
        public void error(ErrorHandler handler, String message, Throwable error, ErrorCategory catagory){
            handler.criticalError(message, error, catagory);
        }
    };
    public static ErrorLevel severe = new ErrorLevel(){
        @Override
        public void error(ErrorHandler handler, String message, Throwable error, ErrorCategory catagory){
            handler.severeError(message, error, catagory);
        }
    };
    public static ErrorLevel moderate = new ErrorLevel(){
        @Override
        public void error(ErrorHandler handler, String message, Throwable error, ErrorCategory catagory){
            handler.moderateError(message, error, catagory);
        }
    };
    public static ErrorLevel minor = new ErrorLevel(){
        @Override
        public void error(ErrorHandler handler, String message, Throwable error, ErrorCategory catagory){
            handler.minorError(message, error, catagory);
        }
    };
    public static ErrorLevel warning = new ErrorLevel(){
        @Override
        public void error(ErrorHandler handler, String message, Throwable error, ErrorCategory catagory){
            handler.warningError(message, error, catagory);
        }
    };
    public static ErrorLevel log = new ErrorLevel() {
        @Override
        public void error(ErrorHandler handler, String message, Throwable error, ErrorCategory catagory){
            handler.log(message, error, catagory);
        }
    };
    public abstract void error(ErrorHandler handler, String message, Throwable error, ErrorCategory catagory);
}
