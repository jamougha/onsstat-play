package models.Matcher;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.MatchResult;
import models.ReducedColumns;
import play.Logger;
import play.db.DB;

/** Does database interaction and title caching to interface
 *  with the TokenMatcher datastructure.
 */

public class DataCache {
   private static DataCache instance;
   private static final boolean DEVEL = true;
   private final TokenMatcher matcher = new TokenMatcher();
   private final Map<Integer, String> datasetsById = new HashMap<>();
   
   private void insert(String token, Datacolumn data) {
      matcher.insert(token, data);
   }
   private DataCache() {
      List<ReducedColumns> columns = ReducedColumns.find.all();
      
      if (DEVEL)
         columns = columns.subList(0, columns.size()/20);

      try (Connection conn = DB.getConnection()) {
         for (ReducedColumns column : columns) {
            // Fill the data structure with mappings from tokens
            // to Datacolumns
            String query = "SELECT c.name, r.id, r.datasets "
                         + "FROM reduced_columns r, cdids c "
                         + "WHERE r.cdid = c.cdid AND c.cdid = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, column.cdid);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
               insertResultSet(rs, column.cdid);
            }
            
            // Cache the titles of datasets so that the hover-text 
            // can be generated quickly on token lookup
            String query2 = "SELECT id, title FROM datasets";
            PreparedStatement stmt2 = conn.prepareStatement(query2);
            ResultSet drs = stmt.executeQuery();
            
            while (drs.next()) {
               datasetsById.put(drs.getInt(1), drs.getString(2));
            }
            
         }
      } catch (SQLException e) {
         e.printStackTrace();
         Logger.error("In tokenmatcher, while building instance: " + e.toString());
      }
   }
   
   private void insertResultSet(ResultSet rs, String cdid) throws SQLException {
      String name = rs.getString(1);
      int id = rs.getInt(2);
      
      Array dataArray = rs.getArray(3);
      Integer[] datasets = (Integer[])dataArray.getArray();
      short[] dataShorts = new short[datasets.length];
      
      for (int i = 0; i < datasets.length; i++) {
         dataShorts[i] = datasets[i].shortValue();
      }
      
      Datacolumn data = new Datacolumn(cdid, name, id, dataShorts);
      
      for (String token : tokenize(name)) {
         instance.insert(token, data);
      }
      instance.insert(cdid, data);
   }
   
   public List<MatchResult> find(String tokenString) {
      List<Datacolumn> matches = matcher.find(tokenize(tokenString));
      List<MatchResult> results = new ArrayList<>();

      
      for (Datacolumn match : matches) {
         StringBuilder titles = new StringBuilder();
         for (int dataset : match.datasets) {
            titles.append(datasetsById.get(dataset));
         }
         results.add(new MatchResult(match.cdid, match.name, match.id, titles.toString()));
      }
      
      return results;      
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
   
   synchronized public static DataCache getInstance() {
      if (instance == null) {
         instance = new DataCache();
      }
      return instance;

   }
}
