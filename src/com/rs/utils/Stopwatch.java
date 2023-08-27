package com.rs.utils;

import java.io.Serializable;

/**
 * @author Simplex
 * @since Nov 18, 2020
 */
public class Stopwatch implements Serializable {

    public Stopwatch() {
        delay(0);
    }
    private static final long serialVersionUID = -110116946186332295L;

    private long end = 0L;

    public void reset() {
        end = 0;
    }

    public Stopwatch delay(int ticks) {
        end = Utils.currentTimeMillis() + (ticks * 600);
        return this;
    }

    public Stopwatch delayMS(long ms) {
        end = Utils.currentTimeMillis() + ms;
        return this;
    }
    public Stopwatch delayMS(int ms) {
        end = Utils.currentTimeMillis() + ms;
        return this;
    }

    public void delaySeconds(int seconds) {
        delay(seconds * 1000);
    }

    public boolean isDelayed() {
        return remaining() > 0;
    }

    public int remaining() {
        return (int) (end - Utils.currentTimeMillis());
    }

    public boolean isDelayed(int extra) {
        return remaining(extra) > -1;
    }

    public int remaining(int extra) {
        return (int) ((end + extra) - Utils.currentTimeMillis());
    }

    public boolean finished() {
        return !isDelayed();
    }
}
