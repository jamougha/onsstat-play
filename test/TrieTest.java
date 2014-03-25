import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import models.Matcher.Trie;
import models.Matcher.TrieResult;

public class TrieTest {


   @Test
   public void testBasicInsertion() {
      Trie<Integer> st = new Trie<Integer>();
      st.put("FOO", 1);
      st.put("FOOBAR1", 2);
      st.put("FOODER", 2);
      st.put("BAZ", 3);
      TrieResult<Integer> foos = st.get("FO");
      
      assertEquals("Searching for a string with no exact matches "
                 + "should return no exact matches", foos.exact.size(), 0);
      
      assertEquals("If we insert two matching strings we get two inexact matches", 
                      foos.partial.size(), 2);
   }

   @Test
   public void testNumericMatching() {
      Trie<Integer> st = new Trie<Integer>();
      st.put("1FOO", 1);
      st.put("1FOOBAR1", 2);
      st.put("2FOODER", 3);
      st.put("1BAZ", 4);
      TrieResult<Integer> foos = st.get("1FOO");
      
      Set<Integer> one = new HashSet<Integer>();
      one.add(1);
      assertEquals("Searching for a string with an exact match "
                 + "should return that match", foos.exact, one);
      
      Set<Integer> two = new HashSet<Integer>();
      two.add(2);
      assertEquals("A search should find the relevant match and no others", 
                      foos.partial, two);
   }
}
