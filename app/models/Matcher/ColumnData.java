package models.Matcher;

import java.util.Arrays;

public class ColumnData {
   public final String cdid;
   public final String name;
   public final int id;
   public final String datasets;
   
   public ColumnData(String c, String n, int i, String d) {
      cdid = c;
      name = n;
      id = i;
      datasets = d;
   }

   @Override
   public int hashCode() {
      return id;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ColumnData other = (ColumnData) obj;
      if (id != other.id)
         return false;
      return true;
   }

}