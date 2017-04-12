package vincent.rpi.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class GpioCommonImpl implements GpioCommon {
    private static final Logger logger = LoggerFactory.getLogger(GpioCommonImpl.class);

    private final GpioController gpio;

    public GpioCommonImpl() {
        this.gpio = GpioFactory.getInstance();
    }

    public GpioPinDigitalOutput activatePin(int address, PinState defaultState, PinState shutdownState) {
        Pin pinByAddress = RaspiPin.getPinByAddress(address);
        logger.info("turing on address = " + address);
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pinByAddress, defaultState);
        logger.info("pin.getName() = " + pin.getName());
        // set shutdown state for this pin
        pin.setShutdownOptions(true, shutdownState);
        pin.setState(defaultState);
        return pin;
    }

    public GpioPinDigitalOutput activatePin(int address) {
        return activatePin(address,PinState.LOW, PinState.LOW);
    }

}