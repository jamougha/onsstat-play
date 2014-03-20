package models.Matcher;

import java.util.Set;

class STreeResult<T> {
   public final Set<T> exact;
   public final Set<T> partial;
   
   public STreeResult (Set<T> e, Set<T> p) {
      exact = e;
      partial = p;
   }
   
   public static<T> STreeResult<T> make(Set<T> e , Set<T> p) {
      return new STreeResult<T>(e, p);
   }
}
