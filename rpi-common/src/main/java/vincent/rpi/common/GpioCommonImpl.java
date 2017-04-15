package vincent.rpi.common;

import java.util.HashMap;
import java.util.Map;

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
    private final Map<Integer, GpioPinDigitalOutput> activatedPins = new HashMap<>();

    public GpioCommonImpl() {
        this.gpio = GpioFactory.getInstance();
    }

    public GpioPinDigitalOutput activatePin(int address, PinState defaultState, PinState shutdownState) {
        Pin pinByAddress = RaspiPin.getPinByAddress(address);
        logger.info("turning on address = " + address);
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pinByAddress, defaultState);
        logger.info("pin.getName() = " + pin.getName());
        // set shutdown state for this pin
        pin.setShutdownOptions(true, shutdownState);
        pin.setState(defaultState);
        activatedPins.put(address, pin);
        return pin;
    }

    public PinState getPinState(int address) {
        return activatedPins.get(address).getState();
    }

    public PinState togglePinState(int address) {
        GpioPinDigitalOutput activePin = activatedPins.get(address);
        activePin.toggle();
        return activePin.getState();
    }


}