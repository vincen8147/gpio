package vincent.rpi.common;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class MockGpioCommon implements GpioCommon {
    private Map<Integer, GpioPinDigitalOutput> activatedPins = new HashMap<>();


    @Override
    public GpioPinDigitalOutput activatePin(int address, PinState defaultState, PinState shutdownState) {
        MockGpioPinDigitalOutput pinDigitalOutput = new MockGpioPinDigitalOutput(address, defaultState);
        activatedPins.put(address, pinDigitalOutput);
        return pinDigitalOutput;
    }

    @Override
    public PinState getPinState(int address) {
        return activatedPins.get(address).getState();
    }

    @Override
    public void setPinState(int address, PinState state) {
        activatedPins.get(address).setState(state);
    }

    public PinState togglePinState(int address) {
        GpioPinDigitalOutput pin = activatedPins.get(address);
        pin.toggle();
        return pin.getState();
    }
}
