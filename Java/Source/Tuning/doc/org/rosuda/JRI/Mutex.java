/*-- $Copyright (C) 2012-13 Felix Dobslaw$

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished
to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */package org.rosuda.JRI;

/** This class implements a (not so) simple mutex. The initial state of the mutex is unlocked. */
public class Mutex {
    public static boolean verbose=false;

    /** defines the current mutex state */
    private boolean locked=false;

    /** thread that locked this mutex (used for simple deadlock-detection) */
    private Thread lockedBy=null;

    /** locks the mutex. If the mutex is already locked, waits until the mutex becomes free. Make sure the same thread doesn't issue two locks, because that will cause a deadlock. Use {@link #safeLock()} instead if you wish to detect such deadlocks. */
    public synchronized void lock()
    {
        while (locked) {
            if (lockedBy==Thread.currentThread())
                System.err.println("FATAL ERROR: org.rosuda.JRI.Mutex detected a deadlock! The application is likely to hang indefinitely!");
            if (verbose)
                System.out.println("INFO: "+toString()+" is locked by "+lockedBy+", but "+Thread.currentThread()+" waits for release (no timeout)");
            try {
                wait();
            } catch (InterruptedException e) {
                if (verbose)
                    System.out.println("INFO: "+toString()+" caught InterruptedException");
            }
        }
        locked=true;
        lockedBy=Thread.currentThread();
        if (verbose) System.out.println("INFO: "+toString()+" locked by "+lockedBy);
    }

    /** locks the mutex. If the mutex is already locked, waits until the mutex becomes free. Make sure the same thread doesn't issue two locks, because that will cause a deadlock.
        @param to timeout in milliseconds, see {@link #wait()}.
        @return <code>true</code> if the lock was successful, <code>false</code> if not
        */
    public synchronized boolean lockWithTimeout(long to)
    {
        if (locked) {
            if (lockedBy==Thread.currentThread())
                System.err.println("FATAL ERROR: org.rosuda.JRI.Mutex detected a deadlock! The application is likely to hang indefinitely!");
            if (verbose)
                System.out.println("INFO: "+toString()+" is locked by "+lockedBy+", but "+Thread.currentThread()+" waits for release (timeout "+to+" ms)");
            try {
                wait(to);
            } catch (InterruptedException e) {
                if (verbose)
                    System.out.println("INFO: "+toString()+" caught InterruptedException");
            }
        }
        if (!locked) {
            locked=true;
            lockedBy=Thread.currentThread();
            if (verbose) System.out.println("INFO: "+toString()+" locked by "+lockedBy);
            return true;
        }
        if (verbose) System.out.println("INFO: "+toString()+" timeout, failed to obtain lock for "+Thread.currentThread());
        return false;
    }

    /** attempts to lock the mutex and returns information about its success.
        @return 0 if the mutex was locked sucessfully<br>1 if the mutex is already locked by another thread<br>-1 is the mutex is already locked by the same thread (hence a call to {@link #lock()} would cause a deadlock). */
    public synchronized int tryLock()
    {
        if (verbose) System.out.println("INFO: "+toString()+" tryLock by "+Thread.currentThread());
        if (locked) return (lockedBy==Thread.currentThread())?-1:1;
        locked=true;
        lockedBy=Thread.currentThread();
        if (verbose) System.out.println("INFO: "+toString()+" locked by "+lockedBy);
        return 0;
    }

    /** Locks the mutex. It works like {@link #lock()} except that it returns immediately if the same thread already owns the lock. It is safer to use this function rather than {@link #lock()}, because lock can possibly cause a deadlock which won't be resolved.
        @return <code>true</code> is the mutex was successfully locked, <code>false</code> if deadlock was detected (i.e. the same thread has already the lock). */
    public synchronized boolean safeLock()
    {
        if (locked && lockedBy==Thread.currentThread()) {
            if (verbose) System.out.println("INFO: "+toString()+" unable to provide safe lock for "+Thread.currentThread());
            return false;
        }
        lock();
        return true;
    }

    /** Locks the mutex. It works like {@link #lockWithTimeout(long)} except that it returns immediately if the same thread already owns the lock. It is safer to use this function rather than {@link #lockWithTimeout(long)}, because lock can possibly cause a deadlock which won't be resolved.
        @return <code>true</code> is the mutex was successfully locked, <code>false</code> if deadlock was detected or timeout elapsed. */
    public synchronized boolean safeLockWithTimeout(long to)
    {
        if (locked && lockedBy==Thread.currentThread()) {
            if (verbose) System.out.println("INFO: "+toString()+" unable to provide safe lock (deadlock detected) for "+Thread.currentThread());
            return false;
        }
        return lockWithTimeout(to);
    }

    /** unlocks the mutex. It is possible to unlock an unlocked mutex, but a warning may be issued. */
    public synchronized void unlock()
    {
        if (locked && lockedBy!=Thread.currentThread())
            System.err.println("WARNING: org.rosuda.JRI.Mutex was unlocked by other thread than locked! This may soon lead to a crash...");
        locked=false;
        if (verbose) System.out.println("INFO: "+toString()+" unlocked by "+Thread.currentThread());

        // notify just 1 in case more of them are waiting
        notify();
    }

    public String toString()
    {
        return super.toString()+"["+((locked)?"":"un")+"locked"+((!locked)?"":(", by "+((lockedBy==Thread.currentThread())?"current":"another")+" thread"))+"]";
    }
}
