package vincent.rpi.cpufan;

import java.io.IOException;

import com.pi4j.system.SystemInfo;

public class TempMonitor {

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
                    } else if (cpuTemperature < coldTemp && device.isOn())  {
                        device.off();
                    }
                    System.out.println("cpuTemperature = " + cpuTemperature+", deviceOn="+device.isOn());
                } catch (InterruptedException e) {
                    System.err.println("Interrupted CPU Temp Monitor");
                } catch (IOException e) {
                    System.err.println("Trouble reading CPU temperature.");
                    e.printStackTrace(System.err);
                }
            }
        }
    }
}
