package gabriel.gettaxi_driver.models.backend;

import android.content.Context;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import gabriel.gettaxi_driver.models.datasource.List_DBManager;
import gabriel.gettaxi_driver.models.entities.ClientRequest;
import gabriel.gettaxi_driver.models.entities.Driver;

public class DB_ManagerFactory  {

    private static List_DBManager list_dbManager;

    public static List_DBManager getDB_Manager()
    {
        if (list_dbManager == null)
            list_dbManager = new List_DBManager();

        return list_dbManager;
    }
}
