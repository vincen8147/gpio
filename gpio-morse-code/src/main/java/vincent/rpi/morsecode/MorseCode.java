package vincent.rpi.morsecode;

import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo;

/**
 * short mark, dot or "dit" (·) : "dot duration" is one time unit long
 * longer mark, dash or "dah" (–) : three time units long
 * inter-element gap between the dots and dashes within a character : one dot duration or one unit long
 * short gap (between letters) : three time units long
 * medium gap (between words) : seven time units long
 */
public class MorseCode {

    public static final long DOT_DURATION = 200L;
    public static final long DASH_DURATION = 3 * DOT_DURATION;
    public static final long PART_DURATION = DOT_DURATION;
    public static final long LETTER_SPACE_DURATION = 3 * DOT_DURATION;
    public static final long WORD_SPACE_DURATION = 7 * DOT_DURATION;

    public static final byte[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyz".getBytes();
    public static final String[] CODES = ("----- .---- ..--- ...-- ....- ..... -.... --... ---.. ----. .- -... -.-. " +
            "-.. . ..-. --. .... .. .--- -.- .-.. -- -. --- .--. --.- .-. ... - ..- ...- .-- -..- -.-- --..")
            .split(" ");
    public static final Map<Byte, String> MORSE_CODE = new HashMap<>();

    static {
        for (int i = 0; i < CHARS.length; i++) {
            byte b = CHARS[i];
            MORSE_CODE.put(b,CODES[i]);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("started.");
            final GpioController gpio = GpioFactory.getInstance();
            System.out.println("Board Type=" + SystemInfo.getBoardType().name());
            // provision pin #01 as an output pin and turn on
            int address = Integer.parseInt(args[0]);

            Pin pinByAddress = RaspiPin.getPinByAddress(address);
            System.out.println("turing on address = " + address);
            final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(pinByAddress, PinState.LOW);
            System.out.println("pin.getName() = " + pin.getName());
            // set shutdown state for this pin
            pin.setShutdownOptions(true, PinState.LOW);
            pin.low();

            byte[] bytesToPlay = args[1].toLowerCase().getBytes();

            for (Byte b : bytesToPlay) {
                if (' ' == b ) {
                    Thread.sleep(WORD_SPACE_DURATION);
                } else {
                    String code = MORSE_CODE.get(b);
                    System.out.println(b.toString() + " -> " + code + ": ");
                    if (null != code) {
                        byte[] codeByteToPlay = code.getBytes();
                        for (byte codeByte : codeByteToPlay) {
                            if ('-' == codeByte) {
                                System.out.print("-");
                                pin.pulse(DASH_DURATION, true);
                            } else if ('.' == codeByte) {
                                System.out.print(".");
                                pin.pulse(DOT_DURATION, true);
                            }
                            Thread.sleep(PART_DURATION);
                        }
                    }
                    Thread.sleep(LETTER_SPACE_DURATION);
                }
                System.out.println();
            }

            gpio.shutdown();
            System.out.println("done.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
