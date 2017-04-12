package vincent.sprinkler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
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
                            wateringQueue.wait(60000L);
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
                    logger.info("Starting station " + station + " for " + durationMinutes + " minutes.");
                    low(pinAddresses.get(pin));
                    long start = System.currentTimeMillis();
                    try {
                        sleep(durationMinutes * 60000L);
                    } catch (InterruptedException e) {
                        logger.info("Station Interrupted: " + station);
                    } finally {
                        high(pinAddresses.get(pin));
                        long endTime = System.currentTimeMillis() - start;
                        logger.info("Stopping station " + station + " after " + endTime / 1000 + " seconds.");
                    }
                }
            }
        }
    }

    StationControl(JsonNode configuration, GpioCommon gpioCommon) throws SchedulerException {

        // Activate the pins for the configured stations.
        JsonNode stationsConfig = configuration.get("stations");
        for (JsonNode station : stationsConfig) {
            int pin = station.get("pin").asInt();
            int id = station.get("id").asInt();
            stations.put(id, new Station(id, pin, station.get("description").asText()));
            pinAddresses.put(pin, gpioCommon.activatePin(pin, PinState.HIGH, PinState.HIGH));
        }

        // Activate the common pin to "ARM" the stations.
        int commonPin = configuration.get("common").get("pin").asInt();
        gpioCommon.activatePin(commonPin, PinState.HIGH, PinState.HIGH);

        // Start the watering request queue monitoring thread.
        runner = new Runner();

        // For each schedule, build the triggers and jobs.
        SchedulerFactory sf = new StdSchedulerFactory();
        scheduler = sf.getScheduler();
        JsonNode schedulesConfig = configuration.get("schedules");
        for (JsonNode scheduleConfig : schedulesConfig) {
            String cronExpression = scheduleConfig.get("startSchedule").asText();

            JobDetail job = newJob(QueueJob.class).build();
            job.getJobDataMap().put("durations", scheduleConfig.get("durations"));
            job.getJobDataMap().put("description", scheduleConfig.get("description").asText());

            CronTrigger trigger = newTrigger()
                    .forJob(job)
                    .withSchedule(cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(job, trigger);
        }
    }

    public void start() {
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

    public void stop() {
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

    /**
     * When triggered this job will place a WateringDuration into the watering queue.
     */
    private class QueueJob implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            JsonNode durations = (JsonNode) jobDataMap.get("durations");
            logger.info("Starting watering schedule: " + jobDataMap.get("description"));
            for (JsonNode duration : durations) {
                int stationId = duration.get("id").asInt();
                int minutes = duration.get("durationMinutes").asInt();
                wateringQueue.add(new WateringDuration(stationId, minutes));
            }
            synchronized (wateringQueue) {
                wateringQueue.notifyAll();
            }
        }
    }
}
