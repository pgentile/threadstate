package example.threadstate.core.retry;

public class AbortRetryException extends RetryException {

    public AbortRetryException(Throwable cause) {
        super(cause);
    }

}
