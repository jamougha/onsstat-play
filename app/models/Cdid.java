package models;


import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.db.ebean.Model.*;

@Entity
@Table(name="cdids") 
public class Cdid {
   
   @Id
   public String cdid;
   
   public String name;
   
   public static Finder<Long, Cdid> find = 
         new Finder<Long, Cdid>(Long.class,Cdid.class);
   public String getName() {
      return name;
   }
   public String getID() {
      return cdid;
   }
   public static String getName(String id) {
      Cdid t = find.where()
                   .eq("cdid", id)
                   .findList()
                   .get(0);
      return t.name;
            
   }
   
   public static List<Cdid> getAll() {
      return find.all();
            
   }
}
