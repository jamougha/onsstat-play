package models;

public class Datacolumn {
   public final String cdid;
   public final String name;
   public final long id;
   
   Datacolumn(String c, String n, Long i) {
      cdid = c;
      name = n;
      id = i;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((cdid == null) ? 0 : cdid.hashCode());
      result = prime * result + (int) (id ^ (id >>> 32));
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
      Datacolumn other = (Datacolumn) obj;
      if (cdid == null) {
         if (other.cdid != null)
            return false;
      } else if (!cdid.equals(other.cdid))
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