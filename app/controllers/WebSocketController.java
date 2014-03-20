package controllers;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.*;
import models.Cdid;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import models.Datacolumn;
import models.Matcher.TokenMatcher;
import play.libs.F.Promise;
import play.libs.F.Function0;
import play.libs.F.Function;

public class WebSocketController extends Controller {
   static final ObjectMapper mapper = new ObjectMapper();

   static JsonNode datacolumnsToJson(Collection<Datacolumn> data, int ident) {

      JsonNode outdata = mapper.createArrayNode();
      
      for (Datacolumn column : data) {
         ObjectNode element = mapper.createObjectNode();
         element.put("cdid", column.cdid);
         element.put("name", column.name);
         element.put("column_id", column.id);
         ((ArrayNode) outdata).add((JsonNode)element);
      }
      
      JsonNode output = mapper.createArrayNode();
      ((ArrayNode) output).add(ident);
      ((ArrayNode) output).add(outdata);
      
      return output;
   }
   
   static void respond(int ident, String tokens, 
         final WebSocket.Out<JsonNode> out, AtomicLong highIdent) {
      TokenMatcher matcher = TokenMatcher.getInstance();
      try {
         if (tokens.length() < 2)
            return;
         
         List<Datacolumn> columns = matcher.find(tokens);
         
         int i = 0;
         final int chunkSize = 50;
         do {
            Thread.sleep(10);
            if (highIdent.get() > ident) {
               return;
            }
            
            int end = Math.min(i + chunkSize, columns.size());
            List<Datacolumn> chunk = columns.subList(i, end);
            JsonNode output = datacolumnsToJson(chunk, ident);
            
            out.write(output);
            i += chunkSize;
         } while (i < columns.size()); 
         
         JsonNode endSignal = mapper.createArrayNode();
         ((ArrayNode) endSignal).add(ident);
         ((ArrayNode) endSignal).add("end");
         
         out.write(endSignal);
         
      } catch (Exception e) {
         // TODO: log this error
         e.printStackTrace();
         JsonNode j = mapper.createObjectNode();
         ((ObjectNode) j).put("error:", e.toString());
         out.write(j);
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
