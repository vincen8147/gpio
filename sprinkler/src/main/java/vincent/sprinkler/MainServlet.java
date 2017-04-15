package vincent.sprinkler;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import vincent.rpi.common.GpioCommon;

public class MainServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(MainServlet.class);

    private final Template template;
    private final Handlebars handlebars;
    private GpioCommon gpioCommon;
    private StationControl stationControl;

    public MainServlet(GpioCommon gpioCommon, StationControl stationControl) {
        this.gpioCommon = gpioCommon;
        this.stationControl = stationControl;
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
              pin = Integer.parseInt( s.hasNext() ? s.next():"-1");
        }
        PinState pinState = gpioCommon.togglePinState(pin);
        logger.info("Manual pin toggle: "+pin+" state is now: "+pinState);
        writer.println(pinState.getName());
    }

    private String getHtml() throws Exception {
        ObjectNode jsonNode =
                new ObjectMapper().readValue(Startup.class.getResource("/config.json"), ObjectNode.class);
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
