package com.lmax.disruptor;


/**
 * <p>Spin first wait strategy for waiting {@link EventProcessor}s on a barrier.</p>
 *
 * <p>This strategy can be used when throughput and low-latency are not as important as CPU resource.
 * Spins for a given number of times then waits using the configured fallback WaitStrategy.</p>
 * @author timmer
 */
public final class SpinCountBackoffWaitStrategy implements WaitStrategy
{
    private final int spin_tries;
    private final WaitStrategy fallbackStrategy;

    public SpinCountBackoffWaitStrategy(int spinTries,
                                        WaitStrategy fallbackStrategy)
    {
        this.spin_tries = spinTries;
        this.fallbackStrategy = fallbackStrategy;
    }


    @Override
    public long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier)
        throws AlertException, InterruptedException, TimeoutException
    {
        long availableSequence;
        int counter = spin_tries;

        do
        {
            if ((availableSequence = dependentSequence.get()) >= sequence)
            {
                return availableSequence;
            }

            if (0 == --counter)
            {
                return fallbackStrategy.waitFor(sequence, cursor, dependentSequence, barrier);
            }

        } while (true);
    }

    @Override
    public void signalAllWhenBlocking()
    {
        fallbackStrategy.signalAllWhenBlocking();
    }
}
