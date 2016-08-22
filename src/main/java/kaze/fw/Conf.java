package kaze.fw;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kaze.fw.conf.Arg;
import kaze.fw.conf.Yml;

public class Conf {

  private static final Logger log = LoggerFactory.getLogger(Conf.class);
  
  public Yml yml;
  public Server server;
  
  private Conf(Yml yml) {
    this.yml = yml;
    this.server = new Server(yml);
  }

  private static InputStream stream(String path) {
    return Conf.class.getResourceAsStream(path);
  }
  
  public static Conf build() {    
    InputStream defaults = stream("/_conf.yml");
    Yml yml = new Yml(defaults);
    
    InputStream user = stream("/conf.yml");
    if (user != null) yml.pull(user);
    
    String e = Arg.d("kaze.env");
    if (e != null) {
      InputStream envYml = stream(
        "/conf-" + e + ".yml"
      );
      if (envYml != null) yml.pull(envYml);
    }
    
    Arg.push(yml);
    return new Conf(yml);
  }

  public class Server {
    public int
      threadMin, threadMax,
      threadTimeout, httpPort, httpTimeout;
    public String
      httpHost, staticDir, staticPath;
    public Server(Yml y) {
      httpHost = y.val("http.host");
      httpPort = y.intVal("http.port");
      httpTimeout = y.intVal("http.timeout");
      threadMin = y.intVal("thread.min");
      threadMax = y.intVal("thread.max");
      threadTimeout = y.intVal("thread.timeout");
      staticDir = y.val("static.dir");
      staticPath = y.val("static.path");
    }
    public void log() {
      log.info(msg,
        httpHost, httpPort, httpTimeout,
        threadMin, threadMax, threadTimeout,
        staticDir, staticPath);
    }
    private static final String msg = "Server " + 
      "[http: host={}, port={}, timeout={}] " + 
      "[thread: min={}, max={}, timeout={}] " +
      "[static: dir={}, path={}]";
  }
}
