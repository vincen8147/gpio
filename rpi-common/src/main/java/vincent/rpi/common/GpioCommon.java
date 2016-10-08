package vincent.rpi.common;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class GpioCommon {

    private GpioController gpio;

    public GpioCommon(GpioController gpio) {
        this.gpio = gpio;
    }

    public GpioPinDigitalOutput activatePin(int address) {
        Pin pinByAddress = RaspiPin.getPinByAddress(address);
        System.out.println("turing on address = " + address);
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pinByAddress, PinState.LOW);
        System.out.println("pin.getName() = " + pin.getName());
        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);
        pin.low();
        return pin;
    }

}