package vincent.sprinkler;

import java.io.IOException;
import java.util.logging.LogManager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import vincent.rpi.common.GpioCommon;
import vincent.rpi.common.GpioCommonImpl;
import vincent.rpi.common.MockGpioCommon;

public class Startup {

    private static final Logger logger = LoggerFactory.getLogger(Startup.class);

    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(Startup.class.getResourceAsStream("/logging.properties"));

        final GpioCommon gpioCommon;
        if (args.length > 0 && args[0].equals("RUN")) {
            logger.info("Running Production GPIO");
            gpioCommon = new GpioCommonImpl();
        } else {
            logger.info("Running Mock GPIO (add 'RUN' arg to run full version.)");
            gpioCommon = new MockGpioCommon();
        }
        WateringConfiguration config = getConfig();
        StationControl stationControl = new StationControl(config, gpioCommon);
        logger.info("Stations ARMED, sleeping before starting scheduler.");
        Thread.sleep(config.getStartDelay());
        stationControl.start();

        // Create a basic jetty server object that will listen on port 8080.
        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        Server server = new Server(config.getPort());

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
        MainServlet mainServlet = new MainServlet(gpioCommon, stationControl);
        ServletHolder servletHolder = new ServletHolder();
        servletHolder.setServlet(mainServlet);
        handler.addServletWithMapping(servletHolder, "/");

        // Start things up!
        server.start();
        logger.info("Web service started.");

        server.addLifeCycleListener(
                new AbstractLifeCycle.AbstractLifeCycleListener() {
                    @Override
                    public void lifeCycleStopping(LifeCycle event) {
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

    private static WateringConfiguration getConfig() throws IOException {
        return new ObjectMapper().readValue(Startup.class.getResource("/config.json"), WateringConfiguration.class);
    }

}
