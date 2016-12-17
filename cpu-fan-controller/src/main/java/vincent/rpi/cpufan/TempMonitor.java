package vincent.rpi.cpufan;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.system.SystemInfo;

public class TempMonitor {
    private static final Logger logger = Logger.getLogger(TempMonitor.class.getName());

    private long frequency;
    private LogicalDevice device;
    private float hotTemp;
    private float coldTemp;
    private boolean keepRunning;

    public TempMonitor(long frequency, LogicalDevice device, float hotTemp, float coldTemp) {
        this.frequency = frequency;
        this.device = device;
        this.hotTemp = hotTemp;
        this.coldTemp = coldTemp;
    }

    public void start() {
        keepRunning = true;
        new Thread(new MonitorThread()).start();
    }

    public void stop() {
        keepRunning = false;
    }

    private class MonitorThread implements Runnable {
        @Override
        public void run() {
            while (keepRunning) {
                try {
                    Thread.sleep(frequency);
                    float cpuTemperature = SystemInfo.getCpuTemperature();
                    if (cpuTemperature > hotTemp && !device.isOn()) {
                        device.on();
                        logger.info("Turning device ON - Temp = " + cpuTemperature);
                    } else if (cpuTemperature < coldTemp && device.isOn())  {
                        device.off();
                        logger.info("Turning device OFF - Temp = " + cpuTemperature);
                    }
                    logger.fine("cpuTemperature = " + cpuTemperature + ", deviceOn=" + device.isOn());
                } catch (InterruptedException e) {
                    logger.info("Interrupted CPU Temp Monitor");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Trouble reading CPU temperature.", e);
                }
            }
        }
    }
}
