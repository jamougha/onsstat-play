package models;

public class Pair<T, U> {
   public final T left;
   public final U right;
   
   public Pair(T left, U right) {
      this.left = left;
      this.right = right;
   }
   
   public static<A, B> Pair<A, B> make(A a, B b) {
      return new Pair<A, B>(a, b);
   }

}
