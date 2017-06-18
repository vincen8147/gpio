package vincent.sprinkler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.JsonNodeValueResolver;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.context.MethodValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.pi4j.io.gpio.PinState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vincent.rpi.common.GpioCommon;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MainServlet.class);

    private final Template template;
    private final Handlebars handlebars;
    private final GpioCommon gpioCommon;
    private final StationControl stationControl;
    private final WateringConfiguration configuration;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MainServlet(GpioCommon gpioCommon, StationControl stationControl, WateringConfiguration configuration) {
        this.gpioCommon = gpioCommon;
        this.stationControl = stationControl;
        this.configuration = configuration;
        TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".hbs");
        handlebars = new Handlebars(loader);
        try {
            template = handlebars.compile("home");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        try {
            writer.println(getHtml());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IllegalStateException("unable to process", e);
        }

    }

    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();

        int pin;
        try (java.util.Scanner s = new java.util.Scanner(request.getInputStream()).useDelimiter("\\A")) {
            pin = Integer.parseInt(s.hasNext() ? s.next() : "-1");
        }
        Station[] stations = configuration.getStations();
        Map<Object, Object> states = new HashMap<>();
        for (Station station : stations) {
            // Turn them all off, then do the toggle.
            // Avoids having more that one on at a time.
            gpioCommon.setPinState(station.getPin(), PinState.HIGH);
            states.put(station.getPin(), PinState.HIGH);
        }
        PinState pinState = gpioCommon.togglePinState(pin);
        states.put(pin, pinState.getName());
        logger.info("Manual pin toggle: " + pin +". State now: "+states.toString());
        objectMapper.writeValue(writer, states);
    }

    private String getHtml() throws Exception {
        ObjectNode jsonNode =
                objectMapper.readValue(Startup.class.getResource("/config.json"), ObjectNode.class);
        JsonNode common = jsonNode.get("common");
        ((ObjectNode) common).put("state", gpioCommon.getPinState(common.get("pin").asInt()).getName());
        JsonNode stations = jsonNode.get("stations");
        for (JsonNode station : stations) {
            int pin = station.get("pin").asInt();
            ((ObjectNode) station).put("state", gpioCommon.getPinState(pin).getName());
        }
        jsonNode.put("queue", Arrays.toString(stationControl.getQueueState()));
        return template.apply(getContext(jsonNode));
    }

    private Context getContext(JsonNode model) {
        return Context.newBuilder(model)
                .resolver(JsonNodeValueResolver.INSTANCE,
                        JavaBeanValueResolver.INSTANCE,
                        FieldValueResolver.INSTANCE,
                        MapValueResolver.INSTANCE,
                        MethodValueResolver.INSTANCE)
                .build();
    }
}
