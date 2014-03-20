import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import models.*;
import models.Matcher.STreeResult;
import models.Matcher.SuffixTree;

public class STreeTest {


   @Test
   public void test() {
      SuffixTree<Integer> st = new SuffixTree<Integer>();
      st.put("foo", 1);
      st.put("foobar", 2);
      st.put("fooder", 2);
      st.put("baz", 3);
      STreeResult<Integer> foos = st.get("fo");
      
      assertEquals("Searching for a string with no exact matches "
                 + "should return no exact matches", foos.exact.size(), 0);
      
      assertEquals("If we insert two matching strings we get two inexact matches", 
                      foos.partial.size(), 2);
   }

}
