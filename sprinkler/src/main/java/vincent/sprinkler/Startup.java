package vincent.sprinkler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.LogManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
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

import vincent.rpi.common.GpioCommon;
import vincent.rpi.common.MockGpioCommon;

public class Startup {

    private static final Logger logger = LoggerFactory.getLogger(Startup.class);

    public static void main(String[] args) throws Exception {
//        System.setProperty("java.util.logging.config.file","logging.properties");
        LogManager.getLogManager().readConfiguration(Startup.class.getResourceAsStream("/logging.properties"));


        // Create a basic jetty server object that will listen on port 8080.
        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        Server server = new Server(8888);

        // The ServletHandler is a dead simple way to create a context handler
        // that is backed by an instance of a Servlet.
        // This handler then needs to be registered with the Server object.
        HandlerCollection handlers = new HandlerCollection();
        server.setHandler(handlers);

        ResourceHandler fileHandler = new ResourceHandler();
        fileHandler.setBaseResource(Resource.newClassPathResource("script"));
        fileHandler.setDirectoriesListed(true);
        handlers.addHandler(fileHandler);

        ServletHandler handler = new ServletHandler();
        handlers.addHandler(handler);

        // Passing in the class for the Servlet allows jetty to instantiate an
        // instance of that Servlet and mount it on a given context path.

        // IMPORTANT:
        // This is a raw Servlet, not a Servlet that has been configured
        // through a web.xml @WebServlet annotation, or anything similar.
        handler.addServletWithMapping(HelloServlet.class, "/index.html");

        // Start things up!
        server.start();

        ObjectNode config = getConfig();
        GpioCommon gpioCommon = new MockGpioCommon();
        StationControl stationControl = new StationControl(config, gpioCommon);
        stationControl.start();

        server.addLifeCycleListener(
                new AbstractLifeCycle.AbstractLifeCycleListener() {
                    @Override
                    public void lifeCycleStopped(LifeCycle event) {
                        stationControl.stop();
                    }
                }
        );

        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See
        // http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
        server.join();

    }

    private static ObjectNode getConfig() throws IOException {
        return new ObjectMapper().readValue(Startup.class.getResource("/config.json"), ObjectNode.class);
    }

    public static class HelloServlet extends HttpServlet {

        private final Template template;
        private final Handlebars handlebars;

        public HelloServlet() {
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
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            writer.println("This is a test");
        }

        private String getHtml() throws Exception {
            ObjectNode jsonNode =
                    new ObjectMapper().readValue(Startup.class.getResource("/config.json"), ObjectNode.class);
            for (JsonNode station : jsonNode.get("stations")) {
                int pin = station.get("pin").asInt();

            }
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
}
