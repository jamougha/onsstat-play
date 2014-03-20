package models.Matcher;

import java.util.Arrays;

public class ColumnData {
   public final String cdid;
   public final String name;
   public final int id;
   public final short[] datasets;
   
   public ColumnData(String c, String n, int i, short[] d) {
      cdid = c;
      name = n;
      id = i;
      datasets = d;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((cdid == null) ? 0 : cdid.hashCode());
      result = prime * result + Arrays.hashCode(datasets);
      result = prime * result + id;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
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
      if (cdid == null) {
         if (other.cdid != null)
            return false;
      } else if (!cdid.equals(other.cdid))
         return false;
      if (!Arrays.equals(datasets, other.datasets))
         return false;
      if (id != other.id)
         return false;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      return true;
   }

}