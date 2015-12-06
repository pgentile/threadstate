package example.threadstate.core.retry;

public class MaxRetryException extends RetryException {

    public MaxRetryException(int count, Throwable cause) {
        super("Failed after " + count + " retries", cause);
    }

}
