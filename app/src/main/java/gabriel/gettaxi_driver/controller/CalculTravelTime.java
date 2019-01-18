package gabriel.gettaxi_driver.controller;

import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Duration;
import com.google.maps.model.TravelMode;

import org.w3c.dom.Text;

import java.io.IOException;

import gabriel.gettaxi_driver.models.entities.ClientRequest;

public class CalculTravelTime extends AsyncTask <Object, Void, Void> {

    @Override
    protected Void doInBackground(Object... objects) {

        ClientRequest clientRequest = (ClientRequest) objects[0];
        TextView textView = (TextView) objects[1];

        String origin, destination;

        origin = clientRequest.getSourceAddress();
        destination = clientRequest.getDestinationAddress();

        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyBFuNRFl9G1TfczjH0ZcrC2-3wwoFYmg3g")
                .build();

        // - Perform the actual request
        DirectionsResult directionsResult = null;

        try {

            if (origin == "NULL" || destination == "NULL")
            {
                textView.setText("Impossible to find the distance");
                return null;
            }

            directionsResult = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(origin)
                    .destination(destination)
                    .await();

        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // - Parse the result
        assert directionsResult != null;
        DirectionsRoute route = directionsResult.routes[0];
        DirectionsLeg leg = route.legs[0];
        Duration duration = leg.duration;

        textView.setText(duration.humanReadable);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

    }
}
