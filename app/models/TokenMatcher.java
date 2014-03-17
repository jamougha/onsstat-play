package models;
      
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class TokenMatcher {
   private static final boolean DEVEL = true;

   private SuffixTree<Datacolumn> tokenMap = new SuffixTree<Datacolumn>();
   private static TokenMatcher instance;
   
   private TokenMatcher() {
      
   }
   
   private void insert(String token, Datacolumn data) {
      tokenMap.put(token, data);
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
      final List<STreeResult<Datacolumn>> tokenMatches = new ArrayList<>();
      Set<Datacolumn> allmatches = null;
      
      for (String token : tokenize(tokens)) {
         STreeResult<Datacolumn> result = tokenMap.get(token);
         tokenMatches.add(result);
         
         if (allmatches == null) {
            allmatches = new HashSet<Datacolumn>();
            allmatches.addAll(result.exact);
            allmatches.addAll(result.partial);
         } else {
            Set<Datacolumn> submatches = new HashSet<Datacolumn>(result.exact);
            submatches.addAll(result.partial);
            allmatches.retainAll(submatches);
         }
      }
      assert allmatches != null;
      
      List<Datacolumn> matchlist = new ArrayList(allmatches);
      
      Collections.sort(matchlist, new Comparator<Datacolumn>() {
         public int compare(Datacolumn d, Datacolumn p) {
            int dexacts = 0, dpartials = 0, pexacts = 0, ppartials = 0;
            
            for (STreeResult r : tokenMatches) {
               if (r.exact.contains(d)) 
                  dexacts++;
               if (r.exact.contains(p)) 
                  pexacts++;
               if (r.partial.contains(d)) 
                  dpartials++;
               if (r.partial.contains(p)) 
                  ppartials++;
            }
            
            if (dexacts != pexacts) {
               return pexacts - dexacts;
            } else {
               return ppartials - dpartials;
            }
         }
      });
      
      return matchlist;
   }
   
   synchronized public static TokenMatcher getInstance() {
      if (instance == null) {
         instance = new TokenMatcher();
      
         List<ReducedColumns> columns = ReducedColumns.find.all();
         if (DEVEL)
            columns = columns.subList(0, columns.size()/100);
         
         for (ReducedColumns column : columns) {
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