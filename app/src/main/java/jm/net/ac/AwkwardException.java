package jm.net.ac;

public class AwkwardException extends RuntimeException {
    public AwkwardException(Throwable cause) {
        super(cause);
    }

    public AwkwardException(String message){
        super(message);
    }

    public  AwkwardException(String message, Throwable cause){
        super(message, cause);
    }
}
