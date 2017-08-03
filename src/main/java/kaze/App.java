package kaze;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class App {
  // method to routes
  private static final Map<String, List<Route>> mth2rts = new HashMap<>();

  //-> for init
  ////-> routing (http methods are in "org.eclipse.jetty.http.HttpMethod")
  public static void get(String path, Func f) { add("GET", path, f); }
  public static void post(String path, Func f) { add("POST", path, f); }
  public static void head(String path, Func f) { add("HEAD", path, f); }
  public static void put(String path, Func f) { add("PUT", path, f); }
  public static void options(String path, Func f) { add("OPTIONS", path, f); }
  public static void delete(String path, Func f) { add("DELETE", path, f); }
  public static void trace(String path, Func f) { add("TRACE", path, f); }
  public static void connect(String path, Func f) { add("CONNECT", path, f); }
  public static void move(String path, Func f) { add("MOVE", path, f); }
  public static void proxy(String path, Func f) { add("PROXY", path, f); }
  public static void pri(String path, Func f) { add("PRI", path, f); }
  private static void add(String method, String path, Func f) {
    List<Route> routes = mth2rts.get(method);
    if (routes == null) {
      routes = new ArrayList<>();
      mth2rts.put(method, routes);
    }
    routes.add(
      new Route(Path.of(path), f)
    );
  }
  ////-> json parser (functions)
  @FunctionalInterface public static interface FromJson {
    <T> T exec(String json, Class<T> to);
  }
  @FunctionalInterface public static interface ToJson {
    String exec(Object from);
  }
  public static FromJson fromJson;
  public static ToJson toJson;
  public static void parser(FromJson json2obj, ToJson obj2json) {
    fromJson=json2obj;  toJson=obj2json;
  }

  //-> for runtime
  public static boolean run(
    HttpServletRequest sreq, HttpServletResponse sres
  ) throws Exception {
    List<Route> rts = mth2rts.get(sreq.getMethod());
    if (rts == null) return false;
    Path path = Path.of(sreq);
    Route route = find(path, rts);
    if (route == null) return false;
    Req req = new Req(sreq, path, route);
    Res res = new Res(sres);
    encoding(sreq, sres);
    // TODO before func
    route.func.exec(req, res);
    // TODO after func
    return true;
  }
  private static Route find(Path reqPath, List<Route> from) {
    for (Route r: from) {
      if (match(r.path, reqPath)) return r;
    }
    return null;
  }
  private static boolean match(Path a, Path r) { // a: added, r: request
    if (a.tree.length != r.tree.length) return false;
    for (int i=0; i<a.tree.length; i++) {
      if (a.tree[i].startsWith(":")) continue;
      if (a.tree[i].equals(r.tree[i])) continue;
      return false;
    }
    return true;
  }
  ////-> encoding
  private static void encoding(
      HttpServletRequest req, HttpServletResponse res)
    throws UnsupportedEncodingException
    {
      if (encoding == null) return;
      if (req.getCharacterEncoding() == null) {
        req.setCharacterEncoding(encoding);
      }
      res.setCharacterEncoding(encoding);
    }
  private static final String utf8 = "utf-8";
  public static String encoding = utf8;
}
