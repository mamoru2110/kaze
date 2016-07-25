package kaze.fw;

import java.util.ArrayList;
import java.util.HashMap;

// not thread safe. 
public class Routes {
  
  public HashMap<String, HashMap<String, Route>>
    method2uri = new HashMap<>();
  
  public HashMap<String, ArrayList<Route>>
    method2regex = new HashMap<>();
  
  // for fw init.
  public void add(String method, String uri, Func func) {
    if (uri.contains(":")) addRegex(method, uri, func);
    else addUri(method, uri, func);
  }
  
  private void addRegex(String method, String uri, Func func) {
    ArrayList<Route> regexRoutes = method2regex.get(method);
    if (regexRoutes == null) {
      regexRoutes = new ArrayList<>();
      method2regex.put(method, regexRoutes);
    }
    Route route = Route.fromRegexUri(uri, func);
    regexRoutes.add(route);
    route.log(method);
  }

  private void addUri(String method, String uri, Func func) {
    HashMap<String, Route> uriRoutes = method2uri.get(method);
    if (uriRoutes == null) {
      uriRoutes = new HashMap<>();
      method2uri.put(method, uriRoutes);
    }
    Route route = Route.fromUri(uri, func);
    uriRoutes.put(uri, route);
    route.log(method);
  }

  // for app running.
  public Route get(String method, String uri) {
    
    // resolve from method2uri
    Route route = null;
    HashMap<String, Route> uriRoutes = method2uri.get(method);
    if (uriRoutes != null) route = uriRoutes.get(uri);
    if (route != null) return route;
    
    // resolve from method2regex
    ArrayList<Route> regexRoutes = method2regex.get(method); 
    if (regexRoutes != null) {
      for(Route r : regexRoutes) {
        if (r.uriPattern.matcher(uri).matches()) return r;
      }
    }
    
    // not found
    return null;
  }  
}