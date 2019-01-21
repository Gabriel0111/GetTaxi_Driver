package gabriel.gettaxi_driver.models.backend;

import java.util.ArrayList;
import java.util.List;

import gabriel.gettaxi_driver.models.datasource.List_DBManager;
import gabriel.gettaxi_driver.models.entities.ClientRequest;
import gabriel.gettaxi_driver.models.entities.Driver;

public interface DB_Manager {
    void addDriver (Driver driver);
    void updateDriver (Driver driver);
    List<Driver> getDrivers();
    void setDrivers(ArrayList<Driver> driverList);
    Driver getCurrentDriver();
    void setCurrentDriver(Driver driver);

    void setMode(String mode);
    String getMode();

    void updateClientRequest(ClientRequest clientRequest);
    List<ClientRequest> getClientRequests();
    List<ClientRequest> getHistoryClients();
    void setClientRequests(ArrayList<ClientRequest> driverList);
    void calculPrice (double distance, ClientRequest clientRequest);

    public void notifyToClientList(final NotifyDataChange<ClientRequest> notifyDataChange);

    public void stopNotifyToClientList();

   // public void notifyToDriverList(final NotifyDataChange<Driver> notifyDataChange);

    public interface NotifyDataChange<T>
    {
        void OnDataChanged(T obj);

        void OnDataAdded(T obj);

        void OnFailure(Exception exception);
    }

    public interface Backend
    {
        public void addDriver(Driver d);

        public interface Action<T>
        {
            void OnSuccess(T obj);

            void OnProgress(String status,double percent);

            void OnFailure(Exception exception);
        }

    }

//    public interface Action<T>
//    {
//        void onSuccess(T obj);
//        void onFailure(Exception exception);
//        void onProgress(String status, double percent);
//    }
//
//    public interface NotifyDataChange<T>
//    {
//        void OnDataChanged(T obj);
//        void onFailure(Exception exception);
//    }
}
