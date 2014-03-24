package controllers;

import play.Logger;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.*;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import java.util.concurrent.atomic.AtomicLong;

import models.Matcher.ColumnData;
import models.Matcher.DataCache;
import play.libs.F.Promise;
import play.libs.F.Function0;
import play.libs.F.Function;

/** Handles the WebSocket interaction for live search.
 *  Each client has a persistent connection. Incoming requests are 
 *  of the form {ident: <int>, message: <string>}. The ident is used
 *  as a response header. The message is simply the search string the user
 *  typed in.
 *  Async is managed with Promises, although these are used for side-effects
 *  (writing to the websocket output) rather than the return type. Each 
 *  connection maintains one Promise and new queries are mapped onto it, ensuring
 *  determinism of message ordering.
 *  Because a query may result in thousands of results, the results are broken 
 *  into chunks. After each chunk is sent the thread checks whether a higher 
 *  ident has been received using the highIdent object and returns if so, 
 *  passing control to the next function mapped onto the Promise.
 *  Each chunk is a Json array. The first element is the ident, the second
 *  is another array. This second array contains objects containing the cdid,
 *  name of the cdid, the id from the reduced_columns table and a string for 
 *  the titles of the datasets. 
 *  Unfortunately Play! seems to have some throuput issues with WebSockets and
 *  so providing a full list of the titles of the datasets is an issue.
 */

public class WebSocketController extends Controller {
   static final ObjectMapper mapper = new ObjectMapper();
   static final int CHUNK_SIZE = 50;
   static final int MIN_SEARCH_LENGTH = 2;

   
   private static JsonNode datacolumnsToJson(Collection<ColumnData> data, int ident) {
      JsonNode outdata = mapper.createArrayNode();
      
      for (ColumnData column : data) {
         ObjectNode element = mapper.createObjectNode();
         element.put("cdid", column.cdid);
         element.put("name", column.name);
         element.put("column_id", column.id);
         element.put("titles", column.datasets);
         ((ArrayNode) outdata).add((JsonNode)element);
      }
      
      JsonNode output = mapper.createArrayNode();
      ((ArrayNode) output).add(ident);
      ((ArrayNode) output).add(outdata);
      
      return output;
   }
   
   private static void respond(int ident, String tokens, 
         final WebSocket.Out<JsonNode> out, AtomicLong highIdent) {
      DataCache matcher = DataCache.getInstance();
      try {
         if (tokens.length() < MIN_SEARCH_LENGTH)
            return;
         
         List<ColumnData> columns = matcher.matchTokens(tokens);
         
         int i = 0;
         // Divide the data into chunks, render each chunk and send
         do {
            int end = Math.min(i + CHUNK_SIZE, columns.size());
            List<ColumnData> chunk = columns.subList(i, end);
            
            JsonNode output = datacolumnsToJson(chunk, ident);
            
            out.write(output);
            i += CHUNK_SIZE;

            // try to give the main thread a chance to accept a new message 
            Thread.yield();
            // and if we've received a query sent after this one, return
            if (highIdent.get() > ident) { 
               return;
            }
         } while (i < columns.size()); 
         
         JsonNode endSignal = mapper.createArrayNode();
         ((ArrayNode) endSignal).add(ident);
         ((ArrayNode) endSignal).add("end");
         
         out.write(endSignal);
         
      } catch (Exception e) {
         e.printStackTrace();
         Logger.error("In WebSocketController.respond: " + e.toString());
      }
   }
   
   public static WebSocket<JsonNode> fetchtokens() {
      return new WebSocket<JsonNode>() {    
        public void onReady(final WebSocket.In<JsonNode> in, 
                            final WebSocket.Out<JsonNode> out) {
          in.onMessage(new Callback<JsonNode>() {
             
             final AtomicLong highIdent = new AtomicLong();

             final Promise<Boolean> exec = Promise.promise( new Function0<Boolean>() {
                public Boolean apply() {
                   return true; // needed to satisfy the Promise interface
                }
             });
             
             public void invoke(JsonNode event) {
                final String tokens = event.findValue("tokens").asText();
                final int ident = event.findValue("ident").asInt();
                highIdent.set(ident);
                
                exec.map( new Function<Boolean, Boolean>() {
                   public Boolean apply(Boolean b) {
                      respond(ident, tokens, out, highIdent);
                      return true; // needed to satisfy the Promise interface
                   }
                });
                
             }
          });
          
          in.onClose(new Callback0() {
             public void invoke() {}
          });
        }
        
      };
    }
}
