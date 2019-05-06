package vincent.rpi.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinShutdown;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListener;

public class MockGpioPinDigitalOutput implements GpioPinDigitalOutput {
    private PinState pinState;
    private int address;

    MockGpioPinDigitalOutput(int address, PinState defaultState) {
        this.pinState = defaultState;
        this.address = address;
        System.out.println(this+": constructed.");
    }

    @Override
    public void high() {
        pinState = PinState.HIGH;
        System.out.println(this + ": PIN set to HIGH");
    }

    @Override
    public void low() {
        pinState = PinState.LOW;
        System.out.println(this + ": PIN set to LOW");
    }

    @Override
    public void toggle() {
        pinState = pinState.isHigh()
                ? PinState.LOW
                : PinState.HIGH;
    }

    @Override
    public String toString() {
        String sb = "MockGpioPinDigitalOutput{" + "pinState=" + pinState
                + ", address=" + address
                + '}';
        return sb;
    }

    @Override
    public Future<?> blink(long delay) {
        return null;
    }

    @Override
    public Future<?> blink(long delay, PinState blinkState) {
        return null;
    }

    @Override
    public Future<?> blink(long delay, long duration) {
        return null;
    }

    @Override
    public Future<?> blink(long delay, long duration, PinState blinkState) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration, Callable<Void> callback) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration, boolean blocking) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration, boolean blocking, Callable<Void> callback) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration, PinState pulseState) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration, PinState pulseState, Callable<Void> callback) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration, PinState pulseState, boolean blocking) {
        return null;
    }

    @Override
    public Future<?> pulse(long duration, PinState pulseState, boolean blocking, Callable<Void> callback) {
        return null;
    }

    @Override
    public Future<?> blink(long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> blink(long l, PinState pinState, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> blink(long l, long l1, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> blink(long l, long l1, PinState pinState, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, Callable<Void> callable, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, boolean b, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, boolean b, Callable<Void> callable, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, PinState pinState, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, PinState pinState, Callable<Void> callable, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, PinState pinState, boolean b, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public Future<?> pulse(long l, PinState pinState, boolean b, Callable<Void> callable, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public void setState(PinState state) {
        pinState = state;
    }

    @Override
    public void setState(boolean state) {
        pinState = state
                ? PinState.HIGH
                : PinState.LOW;
    }

    @Override
    public boolean isHigh() {
        return pinState.isHigh();
    }

    @Override
    public boolean isLow() {
        return pinState.isLow();
    }

    @Override
    public PinState getState() {
        return pinState;
    }

    @Override
    public boolean isState(PinState state) {
        return pinState.equals(state);
    }

    @Override
    public GpioProvider getProvider() {
        return null;
    }

    @Override
    public Pin getPin() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return Integer.toString(this.address);
    }

    @Override
    public void setTag(Object tag) {

    }

    @Override
    public Object getTag() {
        return null;
    }

    @Override
    public void setProperty(String key, String value) {

    }

    @Override
    public boolean hasProperty(String key) {
        return false;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return null;
    }

    @Override
    public void removeProperty(String key) {

    }

    @Override
    public void clearProperties() {

    }

    @Override
    public void export(PinMode mode) {

    }

    @Override
    public void export(PinMode mode, PinState defaultState) {

    }

    @Override
    public void unexport() {

    }

    @Override
    public boolean isExported() {
        return false;
    }

    @Override
    public void setMode(PinMode mode) {

    }

    @Override
    public PinMode getMode() {
        return null;
    }

    @Override
    public boolean isMode(PinMode mode) {
        return false;
    }

    @Override
    public void setPullResistance(PinPullResistance resistance) {

    }

    @Override
    public PinPullResistance getPullResistance() {
        return null;
    }

    @Override
    public boolean isPullResistance(PinPullResistance resistance) {
        return false;
    }

    @Override
    public Collection<GpioPinListener> getListeners() {
        return null;
    }

    @Override
    public void addListener(GpioPinListener... listener) {

    }

    @Override
    public void addListener(List<? extends GpioPinListener> listeners) {

    }

    @Override
    public boolean hasListener(GpioPinListener... listener) {
        return false;
    }

    @Override
    public void removeListener(GpioPinListener... listener) {

    }

    @Override
    public void removeListener(List<? extends GpioPinListener> listeners) {

    }

    @Override
    public void removeAllListeners() {

    }

    @Override
    public GpioPinShutdown getShutdownOptions() {
        return null;
    }

    @Override
    public void setShutdownOptions(GpioPinShutdown options) {

    }

    @Override
    public void setShutdownOptions(Boolean unexport) {

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state) {

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance) {

    }

    @Override
    public void setShutdownOptions(Boolean unexport, PinState state, PinPullResistance resistance, PinMode mode) {

    }
}
