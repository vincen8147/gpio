package vincent.rpi.common;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class MockGpioCommon implements GpioCommon {
    @Override
    public GpioPinDigitalOutput activatePin(int address, PinState defaultState, PinState shutdownState) {
        return new MockGpioPinDigitalOutput(address, defaultState);
    }
}
