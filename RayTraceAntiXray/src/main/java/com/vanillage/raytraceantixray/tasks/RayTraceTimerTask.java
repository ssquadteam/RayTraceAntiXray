package com.vanillage.raytraceantixray.tasks;

import com.vanillage.raytraceantixray.RayTraceAntiXray;
import com.vanillage.raytraceantixray.util.TimeFormatting;

import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class RayTraceTimerTask extends TimerTask {

    private final RayTraceAntiXray plugin;

    public RayTraceTimerTask(RayTraceAntiXray plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            final boolean timings = plugin.isTimingsEnabled();
            final long startTime = timings ? System.nanoTime() : 0L;

            plugin.getExecutorService().invokeAll(plugin.getPlayerData().values().stream().map(pd -> pd.getCallable()).collect(Collectors.toList()));

            if (timings) {
                plugin.getLogger().info((TimeFormatting.format(TimeUnit.NANOSECONDS, System.nanoTime() - startTime, TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS)) + " per ray trace tick.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RejectedExecutionException e) {

        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "Error thrown while raytracing: ", t);
        }
    }
}
