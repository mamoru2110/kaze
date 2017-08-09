package demo.jetty;

import kaze.App;
import kaze.server.EJetty;

public class ConfiguredServer {
  public static void main(String[] args) {
    App.get("/", (req, res) -> {
      res.html("<p>Hello World</p>");
    });
    App.get("/err", (req, res) -> {
      throw new Exception("/err");
    });
    EJetty.location("/public");
    EJetty.session(60);
    EJetty.connector(60000);
    EJetty.thread(10, 10, 50000);
    EJetty.listen("0.0.0.0", 8080);
  }
}
