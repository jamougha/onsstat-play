package models;


import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.db.ebean.Model.*;

@Entity
public class Test {

   @Id
   public Long id;
   
   public String value;
   
   public static Finder<Long, Test> find = 
         new Finder<Long, Test>(Long.class,Test.class);
   
   public static String getValue(Long id) {
      Test t = find.byId(id);
      return t.value;
            
   }
}
