package it.http.tool;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import all.Case;
import kaze.App;
import kaze.lib.Jetty;

public class ItCase extends Case {
  
  protected static final Logger log = LoggerFactory.getLogger(ItCase.class);
  
  static volatile boolean serving = false;
  static final Object lock = new Object();
  static final Thread t = new Thread(
    new Runnable() {
      @Override public void run() {
        App.start(
            "it.http.main",
            "it.http.req",
            "it.http.res"
        );
      }
  }); 
  
  @BeforeClass
  public static void serve() throws InterruptedException {
    synchronized (lock) {
      if (serving) return;
      t.start();
      pause();
      serving = true;
    }
  }

  // for travis ci.
  private static void pause() throws InterruptedException {
    for (int i = 1; i < 6; i++) {
      if (Jetty.started) break;
      lock.wait(1000);
      log.info("Waiting server for {}sec", i);
    }
  }
}
