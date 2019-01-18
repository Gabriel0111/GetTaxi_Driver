package gabriel.gettaxi_driver.controller;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Date;

import gabriel.gettaxi_driver.R;
import gabriel.gettaxi_driver.models.backend.GetTaxiConst;
import gabriel.gettaxi_driver.models.datasource.List_DBManager;
import gabriel.gettaxi_driver.models.entities.ClientRequest;


public class CurrentTrip extends Fragment implements View.OnClickListener {

    Button btnCallClient, btnEmailClient;

    private OnFragmentInteractionListener mListener;

    public CurrentTrip() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {

        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_trip, container, false);

//        btnCallClient = view.findViewById(R.id.current_trip_btnCallClient);
//        btnEmailClient = view.findViewById(R.id.current_trip_btnEmailClient);

        btnCallClient.setOnClickListener(this);
        btnEmailClient.setOnClickListener(this);

        findViews(view);

        return view;
    }

    void findViews(View view)
    {
        ClientRequest chosenClientRequest = List_DBManager.currentClient;

        if (chosenClientRequest == null)
        {
            createSimpleAlertDialog("Ooops. You must choose a client before enter here");
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_current_trip, new AllTrips());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            return;
        }

        TextView clientName = view.findViewById(R.id.presentCurC_nameClient);
        TextView clientStatus = view.findViewById(R.id.presentCurC_status);
        TextView clientSourceAddress = view.findViewById(R.id.presentCurC_sourceAddress);
        TextView clientDestinationAddress = view.findViewById(R.id.presentCurC_destinationAddress);
        TextView clientTravelTime = view.findViewById(R.id.presentCurC_travelTime);
        TextView clientDepartureTime = view.findViewById(R.id.presentCurC_departureTime);
        TextView clientArrivalTime = view.findViewById(R.id.presentCurC_arrivalTime);
        ImageView clientIconStatus = view.findViewById(R.id.presentCurC_iconState);
        TextView clientTravelDistance = view.findViewById(R.id.presentCurC_travelDistance);
        TextView clientTravelPrice = view.findViewById(R.id.presentCurC_price);
     //   Button acceptClient = view.findViewById(R.id.current_trip_btnCallClient);

        Drawable iconAwaiting = getResources().getDrawable(R.drawable.ic_trip_awaiting);
        Drawable iconInTreatment = getResources().getDrawable(R.drawable.ic_trip_treatment);

        Drawable iconEnded = getResources().getDrawable(R.drawable.ic_trip_ended);

        clientName.setText(chosenClientRequest.getClientName());
        clientSourceAddress.setText(chosenClientRequest.getSourceAddress());
        clientDestinationAddress.setText(chosenClientRequest.getDestinationAddress());
        clientDepartureTime.setText(getStringTime(chosenClientRequest.getDepartureTime()));
        clientArrivalTime.setText(getStringTime(chosenClientRequest.getArrivalTime()));
   //     acceptClient.setOnClickListener(this);

        switch (chosenClientRequest.getClientRequestStatus())
        {
            case AWAITING:
                clientStatus.setText("Awaiting");
                clientIconStatus.setImageDrawable(iconAwaiting);
                break;
            case IN_TREATMENT:
                clientStatus.setText("In Treatment");
                clientIconStatus.setImageDrawable(iconInTreatment);
                break;
            case ENDED:
                clientStatus.setText("Ended");
                clientIconStatus.setImageDrawable(iconEnded);
                break;
        }

        clientTravelPrice.setText(chosenClientRequest.getTravelPrice() + " ₪");

        int travelTime = (int) chosenClientRequest.getTravelDistance();
        String result = travelTime > 1000 ? travelTime/1000 + " km" : travelTime + " m";
        clientTravelDistance.setText(result);

        calculTravelTime(chosenClientRequest, clientTravelTime);
    }


    String getStringTime(Date date)
    {
        return String.valueOf(date.getHours()) + "h" +
                ((date.getMinutes() != 0) ? (String.valueOf(date.getMinutes())) : "");
    }

    void calculTravelTime(ClientRequest clientRequest, TextView distanceTxtV)
    {
        CalculTravelTime task =  new CalculTravelTime();
        Object[] object = new Object[3];
        object[0] = clientRequest.getSourceAddress();
        object[1] = clientRequest.getDestinationAddress();
        object[2] = distanceTxtV;
        task.execute(object);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        ClientRequest currentClient = List_DBManager.currentClient;
        if (v == btnCallClient)
        {
            intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + currentClient.getPhoneNumber()));
            startActivity(intent);
        }

        else if (v == btnEmailClient)
        {
            String subject = "Hello " + currentClient.getClientName() + " I'm " +
                    List_DBManager.currentDriver.getFirstName() + " " +  List_DBManager.currentDriver.getLastName() + '\n' +
                    "I'ld like to tell you something:\n";
            intent = new Intent(Intent.ACTION_SENDTO)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_EMAIL, currentClient.getEmail())
                    .putExtra(Intent.EXTRA_SUBJECT, "Your Travel with GetTaxi")
                    .putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }

    private void createSimpleAlertDialog(String message)
    {
        AlertDialog.Builder creationOK = new AlertDialog.Builder(getContext());
        creationOK.setTitle(R.string.app_name);
        creationOK.setMessage(message);
        creationOK.setPositiveButton("OK", null);
        creationOK.show();
    }

}
