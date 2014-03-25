package models.Matcher;

import java.util.Set;

public class TrieResult<T> {
   public final Set<T> exact;
   public final Set<T> partial;
   
   public TrieResult (Set<T> e, Set<T> p) {
      exact = e;
      partial = p;
   }
   
   public static<T> TrieResult<T> make(Set<T> e , Set<T> p) {
      return new TrieResult<T>(e, p);
   }
}
