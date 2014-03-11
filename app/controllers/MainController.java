package controllers;

import play.mvc.*;
import play.*;
import play.libs.F.*;
import play.db.*;
import javax.sql.DataSource;
import models.*;
import play.db.ebean.*;
import java.util.List;
import play.data.*;
import play.db.ebean.Model.*;

public class MainController extends Controller {
    DataSource ds;
    
    public MainController() {
       super();
       ds = DB.getDataSource();
    }
    
    public static Result index() {
        return ok(views.html.index.render());
    }
    
    public static Result getColumn(Long id) {
       return ok(ReducedColumns.getData(id));
    }
    
    
    public static WebSocket<String> fetchtokens() {
       return new WebSocket<String>() {
           
         // Called when the Websocket Handshake is done.
         public void onReady(WebSocket.In<String> in, final WebSocket.Out<String> out) {
           
           // For each event received on the socket,
           in.onMessage(new Callback<String>() {
              WebSocket.Out<String> ret = out;
              public void invoke(String event) {
                  
                // Log events to the console
                ret.write(event);  
                  
              }
           });
           
           // When the socket is closed.
           in.onClose(new Callback0() {
              public void invoke() {
                  
                System.out.println("Disconnected");
                  
              }
           });
           
           // Send a single 'Hello!' message
           out.write("Hello!");
           
         }
         
       };
     }
}
