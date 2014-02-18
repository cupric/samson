//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import com.google.common.base.Preconditions;

import playn.core.PlayN;
import static samson.Log.log;

/**
 * An interface for doing operations after some delay. Allows expiration to occur on a specific
 * thread, and guarantees that any queued expiration will not run if the Interval has since been
 * cancelled or rescheduled.
 */
public abstract class Interval
{
    /**
     * Executor that posts to playn's {@code RunQueue}, {@link PlayN#invokeLater(Runnable)}.
     */
    public static final Executor PLAYN = new Executor () {
        @Override
        public void execute (Runnable runnable) {
            PlayN.invokeLater(runnable);
        }
    };

    /**
     * An interface that will be implemented by the runnable posted to a RunQueue that can be used
     * to retrieve the original Interval.
     */
    public static interface RunBuddy extends Runnable
    {
        /**
         * Retrieve the Interval that is responsible for posting this RunBuddy to a RunQueue. Most
         * likely used to call toString() on the Interval for logging purposes.
         */
        public Interval getInterval ();

        /**
         * Returns the class name of the interval (valid even if the interval has been cancelled
         * and its reference cleared).
         */
        public String getIntervalClassName ();
    }

    /**
     * Create an Interval that uses {@link PlayN#invokeLater(Runnable)} to run the {@link #expired}
     * method.
     */
    public Interval ()
    {
        this(PLAYN);
    }

    /**
     * Create an Interval that uses the specified {@code Executor} to run the {@link #expired}
     * method.
     */
    public Interval (Executor runQueue)
    {
        _runQueue = Preconditions.checkNotNull(runQueue);
    }

    /**
     * The main method where your interval should do its work.
     */
    public abstract void expired ();

    /**
     * Schedules this interval to execute once at <code>when</code>. Supersedes any previous
     * schedule that this Interval may have had.
     */
    public final void schedule (Date when)
    {
        schedule(when.getTime() - System.currentTimeMillis());
    }

    /**
     * Schedule the interval to execute once, after the specified delay.  Supersedes any previous
     * schedule that this Interval may have had.
     */
    public final void schedule (long delay)
    {
        schedule(delay, 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with fixed-rate scheduling between repeats,
     * with the same delay. Supersedes any previous schedule that this Interval may have had.
     *
     * <p> Note: if a repeating interval is scheduled to post itself to a RunQueue and the target
     * RunQueue is shutdown when the interval expires, the interval will cancel itself and log a
     * warning message. </p>
     */
    public final void schedule (long delay, boolean repeat)
    {
        schedule(delay, repeat ? delay : 0L);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified initial delay and repeat
     * delay with fixed-rate scheduling between repeats. Supersedes any previous schedule that this
     * Interval may have had.
     *
     * <p> Note: if a repeating interval is scheduled to post itself to a RunQueue and the target
     * RunQueue is shutdown when the interval expires, the interval will cancel itself and log a
     * warning message. </p>
     */
    public final void schedule (long initialDelay, long repeatDelay)
    {
        schedule(initialDelay, repeatDelay, true);
    }

    /**
     * Schedule the interval to execute repeatedly with the specified initial delay and repeat
     * delay. Supersedes any previous schedule that this Interval may have had.
     *
     * <p> Note: when an interval expires, it is posted to the executor provided on construction
     * and is subject to any constraints implied by its {@link Executor#execute(Runnable)}
     * method.</p>
     *
     * @param fixedRate - if true, this interval schedules repeated expirations using
     * {@link Timer#scheduleAtFixedRate(TimerTask, long, long)} ensuring that the number of
     * expired calls will match the amount of time elapsed. If false, it uses
     * {@link Timer#schedule(TimerTask, long, long)} which ensures that there will be close to
     * <code>repeateDelay</code> milliseconds between expirations.
     *
     * @exception IllegalArgumentException if fixedRate is false and a RunQueue has been specified.
     * That doesn't make sense because the fixed delay cannot account for the time that the
     * RunBuddy sits on the RunQueue waiting to call expire().
     */
    public final void schedule (long initialDelay, long repeatDelay, boolean fixedRate)
    {
        cancel();
        IntervalTask task = new IntervalTask(this);
        _task = task;

        // try twice to schedule the task- see comment inside the catch
        try {
            scheduleTask(initialDelay, repeatDelay, fixedRate);

        } catch (IllegalStateException ise) {
            // Timer.schedule will only throw this if the TimerThead was shut down.
            // This may happen automatically in Applets, so we need to create a new
            // Timer now. Note that in a multithreaded environment it may be possible
            // to have more than one Timer after this happens. That would be slightly
            // undesirable but should not break anything.
            log.warning("Recreating timer");
            _timer = createTimer();
            scheduleTask(initialDelay, repeatDelay, fixedRate);
        }
    }

    /**
     * Cancel the current schedule, and ensure that any expirations that are queued up but have not
     * yet run do not run.
     */
    public final void cancel ()
    {
        IntervalTask task = _task;
        if (task != null) {
            _task = null;
            task.cancel();
        }
    }

    protected final void scheduleTask (long initialDelay, long repeatDelay, boolean fixedRate)
    {
        if (repeatDelay == 0L) {
            _timer.schedule(_task, initialDelay);
        } else if (fixedRate) {
            _timer.scheduleAtFixedRate(_task, initialDelay, repeatDelay);
        } else {
            _timer.schedule(_task, initialDelay, repeatDelay);
        }
    }

    protected final void safelyExpire (IntervalTask task)
    {
        // only expire the interval if the task is still valid
        if (_task == task) {
            try {
                expired();
            } catch (Throwable t) {
                log.warning("Interval broken in expired() " + this, t);
            }

        } else {
            // If the task has been defanged, we go ahead and try cancelling it again. The reason
            // for this is that it's possible to have a runaway task if two threads call schedule()
            // and cancel() at the same time.
            // 1) ThreadA calls cancel() and gets a handle on taskA, yields.
            // 2) ThreadB calls schedule(), gets a handle on taskA, cancel()s, which sets _task to
            //    null, then sets up taskB, returns.
            // 3) ThreadA resumes, sets _task to null and re-cancels taskA.  taskB is now an active
            //    TimerTask but is not referenced anywhere.  In case this is taskB, we cancel it so
            //    that it doesn't ineffectually expire repeatedly until the JVM exists.
            task.cancel();
        }
    }

    /**
     * Note (log) that we were unable to be posted to our RunQueue because it is no longer running.
     */
    protected void noteRejected ()
    {
        log.warning("Interval posted to shutdown RunQueue. Cancelling.",
            "queue", _runQueue, "interval", this);
    }

    protected static Timer createTimer ()
    {
        return new Timer("samskivert Interval Timer", true);
    }

    /**
     * The task that schedules actually runs the interval.
     */
    protected static class IntervalTask extends TimerTask
    {
        public IntervalTask (Interval interval)
        {
            _interval = interval;
            _intervalClassName = interval.getClass().getName();
        }

        @Override public boolean cancel ()
        {
            // remove the reference back to the interval, allowing the Interval itself
            // to be gc'd even as this Task potentially sits on the Timer queue.
            _interval = null;
            return super.cancel();
        }

        @Override public void run () {
            Interval ival = _interval;
            if (ival == null) {
                return;
            }

            if (_runner == null) { // lazy initialize _runner
                _runner = new RunBuddy() {
                    public void run () {
                        Interval ival = _interval;
                        if (ival != null) {
                            ival.safelyExpire(IntervalTask.this);
                        }
                    }
                    public Interval getInterval () {
                        return _interval;
                    }
                    public String getIntervalClassName () {
                        return _intervalClassName;
                    }
                    @Override
                    public String toString () {
                        Interval ival = _interval;
                        return (ival != null) ? ival.toString() : "(Interval was cancelled)";
                    }
                };
            }

            try {
                ival._runQueue.execute(_runner);
            } catch (Exception e) {
                log.warning("Failed to execute interval on run-queue",
                    "queue", ival._runQueue, "interval", ival, e);
            }
        }

        /** If we are using a RunQueue, the Runnable we post to it. */
        protected RunBuddy _runner;

        /** The interval this task is for. We have this reference back to our interval rather
         * than just being a non-static inner class because when a TimerTask is cancelled
         * it still sits on the Timer's queue until its execution time is reached. We want
         * any references held by the interval to be collectable during this period, so our
         * cancel removes the reference back to the Interval. */
        protected Interval _interval;

        /** The class name of the interval (so that we can identify it after cancellation). */
        protected String _intervalClassName;

    } // end: static class IntervalTask

    /** The RunQueue used to run the expired() method for this Interval, or {@link #RUN_DIRECT} to
     * indicate that the interval should be executed directly on the Inteval timer thread. */
    protected Executor _runQueue;

    /** The task that actually schedules our execution with the static Timer. */
    protected volatile IntervalTask _task;

    /** The daemon timer used to schedule all intervals. */
    protected static Timer _timer = createTimer();
}
