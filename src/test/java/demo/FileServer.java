package demo;

import kaze.server.Jetty;

public class FileServer {
  public static void main(String[] args) {
    Jetty jetty = new Jetty();
    jetty.location("/public");
    jetty.listen(8080);
  }
}