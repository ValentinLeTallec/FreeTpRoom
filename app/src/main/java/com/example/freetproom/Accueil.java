package com.example.freetproom;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDateTime;

public class Accueil extends FragmentActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceuil);
        LocalDateTime date = LocalDateTime.now();

        /* Recuperation des différents boutton */
        Button bDate = findViewById(R.id.bDate);
        Button bHeure1 = findViewById(R.id.bHeure1);
        Button bHeure2 = findViewById(R.id.bHeure2);

        /* Formalisation de l'heure */
        String heure1 = normalise(date.toLocalTime().getHour()) + " h " + normalise(date.toLocalTime().getMinute());
        String heure2 = normalise(date.toLocalTime().plusHours(1).getHour()) + " h " + normalise(date.toLocalTime().getMinute());

        /* Affichage */
        bDate.setText(date.toLocalDate().toString());
        bHeure1.setText(heure1);
        bHeure2.setText(heure2);
    }


    public void lookForFreeRoom(View view) {
        if (haveInternetConnection()) {
            Button bDate = findViewById(R.id.bDate);
            Button bHeure1 = findViewById(R.id.bHeure1);
            Button bHeure2 = findViewById(R.id.bHeure2);

            //Recuperation de l'heure sur les bouttons de manière à s'assurer que l'utilisateur reçoive bien l'information qu'il demande
            String time1 = (String) bHeure1.getText().subSequence(0,2) + bHeure1.getText().subSequence(5,7);
            String time2 = (String) bHeure2.getText().subSequence(0,2) + bHeure2.getText().subSequence(5,7);

            if ( Integer.valueOf(time1) < Integer.valueOf(time2) ){  //aka si time1 est avant time2
                String date1 = bDate.getText() + "T" + time1;
                String date2 = bDate.getText() + "T" + time2;

                //Envoi des dates choisies
                Intent toMain2 = new Intent(this, Main2Activity.class);
                toMain2.putExtra("DATE1", date1);
                toMain2.putExtra("DATE2", date2);
                startActivity(toMain2);
                
            } else {
                Intent toError = new Intent(this, Error.class);
                toError.putExtra("messageErreur", "Veuillez entrer des horaires consécutifs.");
                startActivity(toError);
            }


        } else {
            Intent toError = new Intent(this, Error.class);
            toError.putExtra("messageErreur", "Veuillez vous connecter à internet.");
            startActivity(toError);
        }
    }

    // Utilitaires
    /**Fonction haveInternetConnection : return true si connecté, return false dans le cas contraire*/
    private boolean haveInternetConnection(){
        NetworkInfo network = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return network != null && network.isConnected();
    }

    /**Normailistion d'un entier
     * @param nombre le nombre
     * @return "09" si nombre = 9 et "10" si nombre = 10
     */
    //@ requires nombre <100
    private String normalise(int nombre){

        String nbNormalise;
        if (nombre<10) {
            nbNormalise = "0" + ((Integer)nombre).toString();
        } else {
            nbNormalise = ((Integer)nombre).toString();
        }

        return nbNormalise;
    }

    //Définition des pickers et autres joyeusetés

    //          De Date
    public void showDatePicker(View v) {
        DatePicker newFragment = new DatePicker();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
        String date = ((Integer)year).toString() + "-" + normalise(month + 1) + "-" + normalise(day);
        ((TextView) findViewById(R.id.bDate)).setText(date);
    }



    //       De temps

    private String buttonClickedOn = "none";  //Permet de différentier si on est entrain de définir l'heure1 ou l'heure2

    public void showTimePicker1(View v) {
        TimePicker newFragment = new TimePicker();
        buttonClickedOn = "bHeure1";
        newFragment.show(getSupportFragmentManager(),"timePicker1");
    }

    public void showTimePicker2(View v) {
        TimePicker newFragment = new TimePicker();
        buttonClickedOn = "bHeure2";
        newFragment.show(getSupportFragmentManager(),"timePicker2");
    }

    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {

        String heure =  normalise(hourOfDay) + " h " + normalise(minute);

        if (buttonClickedOn.equals("bHeure1")){
            ((TextView) findViewById(R.id.bHeure1)).setText(heure);
        } else if (buttonClickedOn.equals("bHeure2")){
            ((TextView) findViewById(R.id.bHeure2)).setText(heure);
        }
    }
}
