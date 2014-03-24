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
   
   /** Do the DB work to initialize the cache */
   private DataCache() {
      //need to drop down to JDBC because ebean can't do postgres arrays
      try (Connection conn = DB.getConnection()) {
         // Cache the titles of datasets so that the hover-text 
         // can be generated quickly
         String query2 = "SELECT id, title FROM datasets";
         PreparedStatement stmt2 = conn.prepareStatement(query2);
         ResultSet drs = stmt2.executeQuery();

         while (drs.next()) {
            datasetsById.put(drs.getInt(1), drs.getString(2));
         }
         // Fill the data structure with mappings from tokens
         // to Datacolumns
         String query = "SELECT c.cdid, c.name, r.id, r.datasets "
                      + "FROM reduced_columns r, cdids c "
                      + "WHERE r.cdid = c.cdid";
         
         PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery();

         while (rs.next()) {
            ColumnData elem = toColumnData(rs);
            for (String token : tokenize(elem.name)) {
               matcher.insert(token, elem);
            }
            matcher.insert(elem.cdid, elem);
         }

            
      } catch (SQLException e) {
         e.printStackTrace();
         Logger.error("In tokenmatcher, while building instance: " + e.toString());
      }
   }
   
   /** takes a JDBC ResultSet and turns it into a ColumnData instance */
   private ColumnData toColumnData(ResultSet rs) throws SQLException {
      String cdid = rs.getString("cdid");
      String name = rs.getString("name");
      int id = rs.getInt("id");
      Array dataArray = rs.getArray("datasets");

      Integer[] datasets = (Integer[])dataArray.getArray();
      
      return new ColumnData(cdid, name, id, titles(datasets));
      
   }
   
   /** Build a string representing the titles of the datasets the 
   *   cdid data is contained in. 
   */
   private String titles(Integer[] datasets) {
      StringBuilder titles = new StringBuilder();
      for (int id : datasets) {
         titles.append(datasetsById.get(id));
         titles.append('\n');
      }
      titles.deleteCharAt(titles.length() - 1);
      
      String s = titles.toString();
      
      return s;
      
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
