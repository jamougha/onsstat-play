package models;
      
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class TokenMatcher {
   private static final boolean DEVEL = false;
   
   public static class Datacolumn {
      public final String cdid;
      public final String name;
      public final Long id;
      
      Datacolumn(String c, String n, Long i) {
         cdid = c;
         name = n;
         id = i;
      }
   }
   
   private Map<String, Set<Datacolumn>> tokenMap = new HashMap<>();
   private static TokenMatcher instance;
   
   private TokenMatcher() {
      
   }
   
   private void insert(String token, Datacolumn data) {
      for (int i = token.length(); i > 1; i--) {
         String suffix = token.substring(0, i);
         Set<Datacolumn> s = tokenMap.get(suffix);
         
         if (s == null) {
            s = new HashSet<Datacolumn>();
            tokenMap.put(suffix, s);
         }
         s.add(data);
      }
   }
   
   public static List<String> tokenize(String tokenString) {
      List<String> tokens = new ArrayList<>();
      String[] maybeTokens = tokenString.toUpperCase().split("[^A-Z]");
      
      for (String token : maybeTokens) {
         if (token != null)
            tokens.add(token);
      }
      
      return tokens;
   }
   
   public Collection<Datacolumn> find(final String tokens) {
      List<Set<Datacolumn>> tokenMatches = new ArrayList<>();
      for (String token : tokenize(tokens)) {
         Set<Datacolumn> submatches = tokenMap.get(token);
         if (submatches != null) 
            tokenMatches.add(tokenMap.get(token));
      }
      
      if (tokenMatches.isEmpty())
         return new ArrayList<Datacolumn>();

      Set<Datacolumn> matches = new HashSet<Datacolumn>(tokenMatches.remove(0)); //new HashSet<>(tokenMatches.get(0));
      
      for (Set<Datacolumn> tokenMatch : tokenMatches) {
         matches.retainAll(tokenMatch);
      }
      
      return matches;
   }
   
   synchronized public static TokenMatcher getInstance() {
      if (instance == null) {
         System.out.println("initializing cache");
         instance = new TokenMatcher();
      
         List<ReducedColumns> columns = ReducedColumns.find.all();
         if (DEVEL)
            columns = columns.subList(0, columns.size()/10);
         
         for (ReducedColumns column: columns) {
            Cdid cdid = Cdid.find.where()
                            .eq("cdid", column.cdid)
                            .findUnique();
            Datacolumn data = new Datacolumn(cdid.cdid, cdid.name, column.id);
            
            for (String token : tokenize(cdid.name)) {
               instance.insert(token, data);
            }
         }
       
      }
      return instance;

   }

}
