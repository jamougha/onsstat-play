package models;


import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.db.ebean.Model.*;

@Entity
@Table(name="reduced_columns") 
public class ReducedColumns {

   public String cdid;
   
   @Id
   public Long id;
   
   public String datacolumn;
   
   public static Finder<Long, ReducedColumns> find = 
         new Finder<Long, ReducedColumns>(Long.class,ReducedColumns.class);
   
   public static String getData(Long id) {
      ReducedColumns t = find.byId(id);
      return t.datacolumn;
            
   }
}
