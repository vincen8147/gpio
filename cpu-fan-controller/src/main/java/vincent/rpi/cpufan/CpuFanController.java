package vincent.rpi.cpufan;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import vincent.rpi.common.GpioCommon;

public class CpuFanController implements LogicalDevice {

    private final GpioPinDigitalOutput pin;
    private final TempMonitor monitor;

    public static void main(String[] args) {
        System.out.println("Starting...");

        if (args.length != 3) {
            System.err.println("Usage: CpuFanController [pinAddress] [frequency] [hotTemp]");
            System.exit(1);
        }

        CpuFanController fanController =
                new CpuFanController(Integer.parseInt(args[0]), Long.parseLong(args[1]), Float.parseFloat(args[2]));
        fanController.start();
    }

    public CpuFanController(int pinAddress, long frequency, float hotTemp) {
        pin = new GpioCommon().activatePin(pinAddress);
        monitor = new TempMonitor(frequency, this, hotTemp);
    }

    public void start() {
        monitor.start();
    }

    @Override
    public void on() {
        pin.high();
    }

    @Override
    public void off() {
        pin.low();
    }

    @Override
    public boolean isOn() {
        return pin.isHigh();
    }
}
