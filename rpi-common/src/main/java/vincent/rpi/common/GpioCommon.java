package vincent.rpi.common;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public interface GpioCommon {

    GpioPinDigitalOutput activatePin(int address, PinState defaultState, PinState shutdownState);

    PinState getPinState(int address);

    PinState togglePinState(int address);

    void setPinState(int address, PinState state);
}
