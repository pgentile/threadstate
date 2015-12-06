package example.threadstate.core.retry;

import java.util.OptionalLong;
import java.util.Random;

@FunctionalInterface
public interface RetryPolicy {

    static RetryPolicy fixedDuration(final long durationMs) {
        return count -> OptionalLong.of(durationMs);
    }

    static RetryPolicy backoff(final long durationMs) {
        return count -> OptionalLong.of((long) Math.pow(2, durationMs * (count - 1)));
    }

    OptionalLong getWaitTime(int count);

    default RetryPolicy withMaxRetryCount(final int max) {
        final RetryPolicy that = this;
        return count -> {
            if (count > max) {
                return OptionalLong.empty();
            }
            return that.getWaitTime(count);
        };
    }

    default RetryPolicy withJitter(final long jitterMs) {
        final Random random = new Random();
        final RetryPolicy that = this;
        return count -> {
            final OptionalLong durationMs = that.getWaitTime(count);
            if (durationMs.isPresent()) {
                return OptionalLong.of((long) (durationMs.getAsLong() + (random.nextDouble() * jitterMs)));
            }
            return OptionalLong.empty();
        };
    }

}
