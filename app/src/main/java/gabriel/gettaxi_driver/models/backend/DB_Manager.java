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
