package vincent.sprinkler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import vincent.rpi.common.GpioCommon;

class StationControl {
    private static final Logger logger = LoggerFactory.getLogger(StationControl.class);
    private final Scheduler scheduler;

    private final Map<Integer, GpioPinDigitalOutput> pinAddresses = new HashMap<>();
    private final Map<Integer, Station> stations = new HashMap<>();
    private final Queue<WateringDuration> wateringQueue = new ConcurrentLinkedQueue<>();
    private final Runner runner;
    private volatile boolean monitorQueue = true;

    private class Runner extends Thread {
        @Override
        public void run() {
            logger.info("Watering Queue Thread Monitor activated.");
            while (monitorQueue) {
                synchronized (wateringQueue) {
                    while (wateringQueue.isEmpty()) {
                        try {
                            logger.debug("Nothing to water, sleeping.");
                            wateringQueue.wait();
                        } catch (InterruptedException e) {
                            logger.debug("watering runner thread interrupted.", e);
                        }
                    }
                }

                WateringDuration next = wateringQueue.poll();
                if (null != next) {
                    Station station = stations.get(next.getStationId());
                    int pin = station.getPin();
                    int durationMinutes = next.getMinutes();
                    Date finishTime = new Date(System.currentTimeMillis() + durationMinutes * 60000L);
                    logger.info("Starting station {} for {} minutes, finishing at {}.", station, durationMinutes,
                            finishTime);
                    low(pinAddresses.get(pin));
                    long start = System.currentTimeMillis();
                    try {
                        sleep(durationMinutes * 60000L);
                    } catch (InterruptedException e) {
                        logger.info("Station Interrupted: " + station);
                    } finally {
                        high(pinAddresses.get(pin));
                        long endTime = System.currentTimeMillis() - start;
                        logger.info("Stopping station {} after {} seconds.", station, endTime / 1000);
                    }
                }
            }
        }
    }

    StationControl(WateringConfiguration configuration, GpioCommon gpioCommon) throws SchedulerException {

        // Activate the pins for the configured stations.
        Station[] stationsConfig = configuration.getStations();
        for (Station station : stationsConfig) {
            int pin = station.getPin();
            int id = station.getId();
            stations.put(id, station);
            pinAddresses.put(pin, gpioCommon.activatePin(pin, PinState.HIGH, PinState.HIGH));
        }

        // Activate the common pin to "ARM" the stations.
        int commonPin = configuration.getCommon().getPin();
        gpioCommon.activatePin(commonPin, PinState.HIGH, PinState.HIGH);

        // Start the watering request queue monitoring thread.
        runner = new Runner();

        // For each schedule, build the triggers and jobs.
        SchedulerFactory sf = new StdSchedulerFactory();
        scheduler = sf.getScheduler();
        WateringSchedule[] schedules = configuration.getSchedules();
        for (WateringSchedule wateringSchedule : schedules) {
            String cronExpression = wateringSchedule.getStartSchedule();

            JobDetail job = newJob(QueueJob.class).build();
            String description = wateringSchedule.getDescription();
            job.getJobDataMap().put("wateringSchedule", wateringSchedule);
            job.getJobDataMap().put("stationControl", this);

            CronTrigger trigger = newTrigger()
                    .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("PST")))
                    .build();

            Date date = scheduler.scheduleJob(job, trigger);
            logger.info("First run for '" + description + "' is " + date);
        }
    }

    void start() {
        try {
            logger.debug("Starting Station Control.");
            wateringQueue.clear();
            scheduler.start();
            monitorQueue = true;
            runner.start();
        } catch (SchedulerException e) {
            throw new IllegalStateException("Unable to start.", e);
        }
    }

    void queueWateringCycle(WateringDuration cycle) {
        wateringQueue.add(cycle);
        synchronized (wateringQueue) {
            wateringQueue.notifyAll();
        }
    }

    WateringDuration[] getQueueState() {
        return wateringQueue.toArray(new WateringDuration[wateringQueue.size()]);
    }


    void stop() {
        try {
            logger.debug("Stopping Station Control.");
            scheduler.standby();
            wateringQueue.clear();
            monitorQueue = false;
            runner.interrupt();
            for (Station station : stations.values()) {
                high(pinAddresses.get(station.getPin()));
            }
        } catch (SchedulerException e) {
            throw new IllegalStateException("Unable to stop.", e);
        }

    }

    private synchronized void high(GpioPinDigitalOutput pin) {
        logger.debug("Set HIGH " + pin.getName());
        pin.high();
    }

    private synchronized void low(GpioPinDigitalOutput pin) {
        logger.debug("Set LOW " + pin.getName());
        pin.low();
    }

}
