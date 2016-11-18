package vincent.rpi.cpufan;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import vincent.rpi.common.GpioCommon;

public class CpuFanController implements LogicalDevice {

    private final GpioPinDigitalOutput pin;
    private final TempMonitor monitor;

    public static void main(String[] args) {
        System.out.println("Starting...");

        if (args.length != 3) {
            System.err.println("Usage: CpuFanController [pinAddress] [frequency] [onTemp] [offTemp]");
            System.exit(1);
        }

        CpuFanController fanController =
                new CpuFanController(parseInt(args[0]), parseLong(args[1]), parseFloat(args[2]), parseFloat(args[3]));
        fanController.start();
    }

    public CpuFanController(int pinAddress, long frequency, float onTemp, float offTemp) {
        pin = new GpioCommon().activatePin(pinAddress);
        monitor = new TempMonitor(frequency, this, onTemp, offTemp);
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
