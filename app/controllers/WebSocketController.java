package controllers;

import models.TokenMatcher;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.*;
import models.Cdid;
import java.util.Collection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.util.Map;

public class WebSocketController extends Controller {

   
   public static WebSocket<JsonNode> fetchtokens() {
      final TokenMatcher matcher = TokenMatcher.getInstance();
      
      return new WebSocket<JsonNode>() {    
        public void onReady(final WebSocket.In<JsonNode> in, 
                            final WebSocket.Out<JsonNode> out) {
          in.onMessage(new Callback<JsonNode>() {
             public void invoke(JsonNode event) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                   String tokens = event.findValue("tokens").asText();
                   if (tokens.length() < 3)
                      return;
                   
                   Collection<TokenMatcher.Datacolumn> columns = matcher.find(tokens);
                   JsonNode outdata = mapper.createArrayNode();
                   
                   for (TokenMatcher.Datacolumn column : columns) {
                      JsonNode element = mapper.createObjectNode();
                      ((ObjectNode) element).put("cdid", column.cdid);
                      ((ObjectNode) element).put("name", column.name);
                      ((ObjectNode) element).put("column_id", column.id);
                      ((ArrayNode) outdata).add(element);
                   }
                   
                   JsonNode output = mapper.createArrayNode();
                   ((ArrayNode) output).add(event.findValue("ident"));
                   ((ArrayNode) output).add(outdata);
                   out.write(output);
                } catch (Exception e) {
                   // TODO: log this error
                   JsonNode j = mapper.createObjectNode();
                   ((ObjectNode) j).put("error:", e.toString());
                   out.write(j);
                }
             }
          });
          
          // When the socket is closed.
          in.onClose(new Callback0() {
             public void invoke() {
                 
               System.out.println("Disconnected");
                 
             }
          });
        }
        
      };
    }
}
