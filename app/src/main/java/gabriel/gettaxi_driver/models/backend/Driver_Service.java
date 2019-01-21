package gabriel.gettaxi_driver.models.backend;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import gabriel.gettaxi_driver.models.entities.ClientRequest;

public class Driver_Service extends Service
{

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        DB_Manager db_manager = DB_ManagerFactory.getDB_Manager();
        db_manager.notifyToClientList(new DB_Manager.NotifyDataChange<ClientRequest>() {
            @Override
            public void OnDataChanged(ClientRequest obj) {
                Intent intent = new Intent();
                intent.putExtra(GetTaxiConst.ClientConst.DESTINATION, obj.getDestinationAddress());
                sendBroadcast(intent);
            }

            @Override
            public void OnDataAdded(ClientRequest obj) {

            }

            @Override
            public void OnFailure(Exception exception) {

            }
        });

        return START_STICKY ;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
