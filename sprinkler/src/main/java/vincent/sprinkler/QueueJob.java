package vincent.sprinkler;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When triggered this job will place the WateringDurations of a schedule into the watering queue.
 */
public class QueueJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(QueueJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        WateringSchedule wateringSchedule = (WateringSchedule) jobDataMap.get("wateringSchedule");
        logger.info("Starting watering schedule: " + wateringSchedule.getDescription());
        StationControl stationControl = (StationControl) jobDataMap.get("stationControl");
        for (WateringDuration duration : wateringSchedule.getDurations()) {
            stationControl.queueWateringCycle(duration);
        }
        Date nextFireTime = jobExecutionContext.getNextFireTime();
        logger.info("Next run for '" + wateringSchedule.getDescription() + "' will start at " + nextFireTime);
    }
}
