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

import models.ReducedColumns;
import play.Logger;
import play.db.DB;

/** Does database interaction and title caching to interface
 *  with the TokenMatcher datastructure, and caches the titles
 *  of the datasets by their id
 */

public class DataCache {
   private static DataCache instance;
   private static final boolean DEVEL = true;
   private final TokenMatcher matcher = new TokenMatcher();
   private final Map<Integer, String> datasetsById = new HashMap<>();
   
   /** Return a list of data for cdids with titles matching the search string
    */
   public List<ColumnData> matchTokens(String tokenString) {
      List<ColumnData> matches = matcher.find(tokenize(tokenString));
      return matches;     
   }
   
   /** Fetch the name of a dataset given its id
    */
   public String datasetName(int id) {
      getInstance(); // initialize everything if we haven't already
      return datasetsById.get(id);
   }
   
   /** Do the DB work to initialize the cache */
   private DataCache() {
      //need to drop down to JDBC because ebean can't do postgres arrays
      try (Connection conn = DB.getConnection()) {
         // Fill the data structure with mappings from tokens
         // to Datacolumns
         String query = "SELECT c.cdid, c.name, r.id, r.datasets "
                      + "FROM reduced_columns r, cdids c "
                      + "WHERE r.cdid = c.cdid";
         
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();
         
         while (rs.next()) {
            insertResultSet(rs);
         }
         // Cache the titles of datasets so that the hover-text 
         // can be generated quickly
         String query2 = "SELECT id, title FROM datasets";
         PreparedStatement stmt2 = conn.prepareStatement(query2);
         ResultSet drs = stmt2.executeQuery();
         while (drs.next()) {
            datasetsById.put(drs.getInt(1), drs.getString(2));
         }
            
      } catch (SQLException e) {
         e.printStackTrace();
         Logger.error("In tokenmatcher, while building instance: " + e.toString());
      }
   }
   
   private void insertResultSet(ResultSet rs) throws SQLException {
      String cdid = rs.getString(1);
      String name = rs.getString(2);
      int id = rs.getInt(3);
      Array dataArray = rs.getArray(4);

      Integer[] datasets = (Integer[])dataArray.getArray();
      short[] dataShorts = new short[datasets.length];
      
      for (int i = 0; i < datasets.length; i++) {
         dataShorts[i] = datasets[i].shortValue();
      }
      
      ColumnData data = new ColumnData(cdid, name, id, dataShorts);
      for (String token : tokenize(name)) {
         matcher.insert(token, data);
      }
      matcher.insert(cdid, data);
   }
   
   private static List<String> tokenize(String tokenString) {
      List<String> tokens = new ArrayList<>();
      String[] maybeTokens = tokenString.toUpperCase().split("[^A-Z0-9]");
      
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
