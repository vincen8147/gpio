package vincent.rpi.cpufan;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import vincent.rpi.common.GpioCommonImpl;

public class CpuFanController implements LogicalDevice {

    private final GpioPinDigitalOutput pin;
    private final TempMonitor monitor;

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: CpuFanController [pinAddress] [frequency] [onTemp] [offTemp]");
            System.err.println("Turns on when temp is above 'onTemp'.  Turns off when temp is below 'offTemp'.");
            System.exit(1);
        }

        CpuFanController fanController =
                new CpuFanController(parseInt(args[0]), parseLong(args[1]), parseFloat(args[2]), parseFloat(args[3]));
        fanController.start();
    }

    public CpuFanController(int pinAddress, long frequency, float onTemp, float offTemp) {
        pin = new GpioCommonImpl().activatePin(pinAddress, PinState.LOW, PinState.LOW);
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
