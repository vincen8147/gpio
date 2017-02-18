package vincent.rpi.simple;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

import vincent.rpi.common.GpioCommon;

public class SimpleGpio {

    private final GpioPinDigitalOutput pin;

    public static void main(String[] args) {
        new SimpleGpio(Integer.parseInt(args[0]), Boolean.parseBoolean(args[1]));
    }

    SimpleGpio(int pinAddress, boolean on) {
        pin = new GpioCommon().activatePin(pinAddress);
        if (on) {
            pin.high();
        } else {
            pin.low();
        }

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
