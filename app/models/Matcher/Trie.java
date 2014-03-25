package models.Matcher;

import java.util.HashSet;
import java.util.Set;

/** Trie datastructure mapping Strings containing only 0-9, A-Z to some datatype
 */

final public class Trie<T> {
   private final class Node<T> {
      final Node<T>[] children = new Node[36];
      final Set<T> elements = new HashSet<T>();
   }
   
   private final Node<T> root = new Node<T>();
   
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
   
   public TrieResult<T> get(String key) {
      Node<T> node = root;
      
      for (int i = 0; i < key.length(); i++) {
         int index = indexOf(key.charAt(i));
         
         if (node.children[index] == null) 
            return new TrieResult<T>(new HashSet<T>(), new HashSet<T>());
         
         node = node.children[index];
      }
      
      Set<T> exacts = new HashSet<>(node.elements);
      Set<T> partials = elementsOfChildren(node, new HashSet<T>());
      
      return TrieResult.make(exacts, partials);
   }

   /** recursively find all elements from the children of a node and
    *  add them to acc.
    */
   private Set<T> elementsOfChildren(Node<T> node, Set<T> acc) {
      for (Node<T> child : node.children) {
         if (child != null) {
            acc.addAll(child.elements);
            elementsOfChildren(child, acc);
         }
      }
      
      return acc;
   }
   
   private int indexOf(char c) {
      int i = (int)c;
      if (48 <= i && i < 58) { // '0' to '9'
         return i - 48;
      } else if (65 <= i && i < 91) { // 'A' to 'Z'
         return i - 55;
      } else {
         throw new IllegalArgumentException(c + " is not in the range 0-9, A-Z");
      }
   }
}