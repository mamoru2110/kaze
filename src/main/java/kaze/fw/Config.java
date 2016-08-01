package kaze.fw;

public class Config {

  public Jetty jetty;
  
  public static Config defaults() {
    Config c = new Config();
    c.jetty = new Jetty();
    return c;
  }
  
  public static class Jetty {
    public int 
      threadMax=200, threadMin=8, threadTimeout=60000,
      httpPort=8080, httpTimeout=30000;
    public String
      httpHost, staticDir, staticPath="/public";
    @Override
    public String toString() {
      StringBuilder s = new StringBuilder();
      return s.append("[thread: max=").append(threadMax)
           .append(", min=").append(threadMin)
           .append(", timeout=").append(threadTimeout)
           .append("] [http: host=").append(httpHost)
           .append(", port=").append(httpPort)
           .append(", timeout=").append(httpTimeout)
           .append("] [static: dir=").append(staticDir)
           .append(", path=").append(staticPath)
           .append("]").toString();
    }
  }
}
