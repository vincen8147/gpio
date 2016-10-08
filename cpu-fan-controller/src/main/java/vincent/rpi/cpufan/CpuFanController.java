package vincent.rpi.cpufan;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import vincent.rpi.common.GpioCommon;

public class CpuFanController {

    public static void main(String[] args) {
        System.out.println("Starting...");

        if (args.length != 1) {
            System.err.println("Usage: CpuFanController [pinAddress]");
            System.exit(1);
        }

        final GpioController gpio = GpioFactory.getInstance();
        GpioCommon gpioCommon = new GpioCommon(gpio);
        GpioPinDigitalOutput pin = gpioCommon.activatePin(Integer.parseInt(args[0]));
        System.out.println("Pulse Starting.");
        pin.pulse(5000L, true);
        System.out.println("Pulse Stopped.");
    }

}
