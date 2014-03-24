package models.Matcher;

import java.util.HashSet;
import java.util.Set;

final class Trie<T> {
   private final class Node<T> {
      final Node<T>[] children = new Node[36];
      final Set<T> elements = new HashSet<T>();
   }
   
   private final Node<T> root = new Node<T>();

   private int indexOf(char c) {
      int i = (int)c;
      if (48 <= i && i < 58) {
         return i - 48;
      } else if (65 <= i && i < 91) {
         return i - 55;
      } else {
         assert false;
         return 0; // satisfy eclipse
      }
   }
   
   public void put(String key, T value) {
      Node<T> node = root;
      for (int i = 0; i < key.length(); i++) {
         int index = indexOf(key.charAt(i));
         
         if (node.children[index] == null)
            node.children[index] = new Node<T>();
         
         node = node.children[index];
      }
      
      node.elements.add(value);
   }
   
   private Set<T> accumulate(Node<T> node, Set<T> acc) {
      for (Node<T> child : node.children) {
         if (child != null) {
            acc.addAll(child.elements);
            accumulate(child, acc);
         }
      }
      
      return acc;
   }
   
   public STreeResult<T> get(String key) {
      Node<T> node = root;
      
      for (int i = 0; i < key.length(); i++) {
         int index = indexOf(key.charAt(i));
         
         if (node.children[index] == null) 
            return new STreeResult<T>(new HashSet<T>(), new HashSet<T>());
         
         node = node.children[index];
      }
      
      Set<T> exacts = new HashSet<>(node.elements);
      Set<T> partials = accumulate(node, new HashSet<T>());
      
      return STreeResult.make(exacts, partials);
   }
}
