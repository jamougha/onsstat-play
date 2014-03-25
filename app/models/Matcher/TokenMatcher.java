package models.Matcher;
      
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import models.ReducedColumns;
import play.Logger;
import play.db.*;

/** Interface to Trie for matching token strings. 
 *  Also handles ordering of results.  
 */

public class TokenMatcher {
   private Trie<ColumnData> tokenMap = new Trie<>();
   
   void insert(String token, ColumnData data) {
      tokenMap.put(token, data);
   }
   
   /** Retrieve ColumnDatas that match the tokens from the
    *  suffix tree. Only ColumnDatas where the name matches all tokens 
    *  are returned. Results are sorted by the ordering in ColumnDataOrdering. 
    */
   public List<ColumnData> find(final List<String> tokens) {
      final List<TrieResult<ColumnData>> tokenMatches = new ArrayList<>();
      Set<ColumnData> allmatches = null;
      
      for (String token : tokens) {
         TrieResult<ColumnData> result = tokenMap.get(token);
         tokenMatches.add(result);
         
         if (allmatches == null) {
            allmatches = new HashSet<ColumnData>();
            allmatches.addAll(result.exact);
            allmatches.addAll(result.partial);
         } else {
            Set<ColumnData> submatches = new HashSet<ColumnData>(result.exact);
            submatches.addAll(result.partial);
            allmatches.retainAll(submatches);
         }
      }
      
      List<ColumnData> matchlist = new ArrayList<>(allmatches);
      
      Collections.sort(matchlist, new ColumnDataOrder(tokenMatches));
      
      return matchlist;
   }
}

/** Ordering over ColumnData instances. Order by
 *  1. How many exact matches were found
 *  2. How many partial matches were found
 *  3. The length of the name of the CDID, descending
 *  4. The name of the CDID
 */
class ColumnDataOrder implements Comparator<ColumnData> {
   final Collection<TrieResult<ColumnData>> tokenMatches;
   
   ColumnDataOrder(Collection<TrieResult<ColumnData>> t) {
      tokenMatches = t;
   }
   @Override
   public int compare(ColumnData d, ColumnData p) {
      int dexacts = 0, dpartials = 0, pexacts = 0, ppartials = 0;
      
      for (TrieResult<ColumnData> r : tokenMatches) {
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
      } else if (d.name.length() != p.name.length()) {
         return d.name.length() - p.name.length();
      } else {
         return p.name.compareTo(d.name);
      }
   }
}