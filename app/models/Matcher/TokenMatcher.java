package models.Matcher;
      
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import models.Datacolumn;
import models.ReducedColumns;
import play.Logger;
import play.db.*;

public class TokenMatcher {
   private static final boolean DEVEL = true;

   private SuffixTree<Datacolumn> tokenMap = new SuffixTree<Datacolumn>();
   private static TokenMatcher instance;
   
   private void insert(String token, Datacolumn data) {
      tokenMap.put(token, data);
   }
   
   
   /* Retrieve Datacolumns that match the tokens from the
    * suffix tree. Only Datacolumns where the name matches all tokens 
    * are returned. Results are sorted by the number of exact matches, then 
    * the number of partial matches. 
    */
   
   public List<Datacolumn> find(final List<String> tokens) {
      final List<STreeResult<Datacolumn>> tokenMatches = new ArrayList<>();
      Set<Datacolumn> allmatches = null;
      
      for (String token : tokens) {
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
      
      List<Datacolumn> matchlist = new ArrayList<>(allmatches);
      
      Collections.sort(matchlist, new Comparator<Datacolumn>() {
         public int compare(Datacolumn d, Datacolumn p) {
            int dexacts = 0, dpartials = 0, pexacts = 0, ppartials = 0;
            
            for (STreeResult<Datacolumn> r : tokenMatches) {
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
            } else if (ppartials != dpartials) {
               return ppartials - dpartials;
            } else if (d.name.length() != p.name.length()){
               return d.name.length() - p.name.length();
            } else {
               return p.name.compareTo(d.name);
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
            columns = columns.subList(0, columns.size()/20);

         try (Connection conn = DB.getConnection()) {
            for (ReducedColumns column : columns) {
               String query = "SELECT c.name, r.id, r.datasets "
                            + "FROM reduced_columns r, cdids c "
                            + "WHERE r.cdid = c.cdid AND c.cdid = ?";
               
               PreparedStatement stmt = conn.prepareStatement(query);
               stmt.setString(1, column.cdid);
               ResultSet rs = stmt.executeQuery();
               
               while (rs.next()) {
                  String name = rs.getString(1);
                  int id = rs.getInt(2);
                  
                  Array dataArray = rs.getArray(3);
                  Integer[] datasets = (Integer[])dataArray.getArray();
                  short[] dataShorts = new short[datasets.length];
                  
                  for (int i = 0; i < datasets.length; i++) {
                     dataShorts[i] = datasets[i].shortValue();
                  }
                  
                  Datacolumn data = new Datacolumn(column.cdid, name, id, dataShorts);
                  
                  for (String token : tokenize(name)) {
                     instance.insert(token, data);
                  }
                  instance.insert(column.cdid, data);
               }
            }
         } catch (SQLException e) {
            e.printStackTrace();
            Logger.error("In tokenmatcher, while building instance: " + e.toString());
         }
       
      }
      return instance;

   }

}
