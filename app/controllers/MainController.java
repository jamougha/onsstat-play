package controllers;

import play.mvc.*;
import play.*;
import play.libs.F.*;
import play.db.*;
import javax.sql.DataSource;
import models.*;
import play.db.ebean.*;
import java.util.List;
import play.data.*;
import play.db.ebean.Model.*;

public class MainController extends Controller {
    DataSource ds;
    
    public static Result index() {
        return ok(views.html.index.render());
    }
    
    public static Result getColumn(Long id) {
       return ok(ReducedColumns.getData(id));
    }
    
    
}
