package kaze.fw.embed;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.access.jetty.RequestLogImpl;
import kaze.Conf;

public class JettyServer {

  private static final Logger log = LoggerFactory.getLogger(JettyServer.class);
  
  private static Conf.Jetty conf;
  private static JettyServlet servlet;
  private static Server server;
  
  public JettyServer(Conf c, JettyServlet s) {
    conf = c.jetty;
    servlet = s;
  }
  
  public void start() {
    log.info("Jetty Config {}", conf.toString());
    server = server();
    try { server.start(); }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  public void listen() {
    try { server.join(); }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

	private Server server() {
		QueuedThreadPool tp = new QueuedThreadPool(
		    conf.threadMax, conf.threadMin, conf.threadTimeout
		);
    Server sv = new Server(tp);
    sv.setConnectors(Connectors.build(sv));
    HandlerCollection hc = new HandlerCollection();
    hc.setHandlers(Handlers.build());
    sv.setHandler(hc);
    return sv;
	}
	
	// Parts of Server ->
	private static class Connectors {
    static Connector[] build(Server sv) {
      HttpConfiguration c = new HttpConfiguration();
      c.setSendServerVersion(false);  // always.
      HttpConnectionFactory fac = new HttpConnectionFactory(c);
      ServerConnector con = new ServerConnector(sv, fac);
      con.setIdleTimeout(conf.httpTimeout);
      con.setHost(conf.httpHost);
      con.setPort(port());
      return new Connector[] { con };
    }
    static int port() {
      String key = "port";
      String p = System.getProperty(key);
      if (p == null) return conf.httpPort;
      log.info(
        "Change http port to system property {} {}",
        "[key=" + key + "]", "[val=" + p + "]"
      );
      return Integer.parseInt(p);
    }
	}
	
	private static class Handlers {
    static Handler[] build() {
      Handler sh = ServletHandler.build();
      Handler lh = LogHandler.build();
      return (lh == null) ?
          new Handler[]{sh} :
          new Handler[]{sh, lh};
    }
	}
	
	private static class ServletHandler {
	  static Handler build() {
      ServletContextHandler h = new ServletContextHandler(
          ServletContextHandler.SESSIONS
      );
      h.setBaseResource(base());
      h.addServlet(servlet(), "/");
      h.setErrorHandler(ErrHandler.build());
      return h;
    }
    static Resource base() {
      if (conf.staticDir != null) {
        return Resource.newResource(
            new File(conf.staticDir)
        );
      }
      return Resource.newClassPathResource(
            conf.staticPath
      );
    } 
    static ServletHolder servlet() {
      ServletHolder h = new ServletHolder(
          "default", servlet
      );
      // always.
      h.setInitParameter("dirAllowed", "false");
      return h;
    }
	}

  // http://logback.qos.ch/access.html
	private static class LogHandler {
    static RequestLogHandler build() {
      String xml =  "/logback-access.xml";
      if (
        LogHandler.class.getResource(xml) == null
      ) return null;
      RequestLogImpl log = new RequestLogImpl();
      log.setResource(xml);
      RequestLogHandler h = new RequestLogHandler();
      h.setRequestLog(log);
      return h;
    }
	}
	
	private static class ErrHandler {
	  static ErrorHandler build() {
	    return new ErrorHandler() {
	      @Override public void handleErrorPage(
	        HttpServletRequest req, Writer writer,
	        int code, String msg) throws IOException
	      {
	        if (msg == null) {
	          msg = HttpStatus.getMessage(code);
	        }
	        writer.write(msg);
	      }
	    };
	  }
	}
}