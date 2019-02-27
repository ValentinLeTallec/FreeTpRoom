package com.example.freetproom;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.filter.PeriodRule;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.util.MapTimeZoneCache;

import android.os.AsyncTask;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Intent toMain2 = getIntent();
        String Date1 = toMain2.getStringExtra("DATE1");
        String Date2 = toMain2.getStringExtra("DATE2");


        String urlCalendar = "https://edt.inp-toulouse.fr/jsp/custom/modules/plannings/anonymous_cal.jsp?resources=304,305,306,307,308,309,311,312,313,314,315,316,938,939,942,947&projectId=42&calType=ical&firstDate=" + Date1.substring(0, 10) + "&lastDate=" + Date2.substring(0, 10);
        URL url;
        try {
            url = new URL(urlCalendar);
        } catch (Exception e) {
            return;  //The URL is manually generated so no problems
        }

        new findFreeRooms(this).execute(new Tuple<>(url, Date1, Date2));
    }


    private static class findFreeRooms extends AsyncTask<Tuple<URL, String, String>, Void, Tuple<Calendar, String, String>> {

        private WeakReference<Main2Activity> activityReference;

        // only retain a weak reference to the activity (allows you to make findFreeRooms static which is better (see warning))
        findFreeRooms(Main2Activity context) {
            activityReference = new WeakReference<>(context);
        }

        @SafeVarargs
        @Override
        protected final Tuple<Calendar, String, String> doInBackground(Tuple<URL, String, String>... params) {
            InputStream fileCal;
            Calendar calendar;
            String date1;
            String date2;

            try {
                // YOLO !!!! aka it works this way ; p
                System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());

                fileCal = params[0].x.openStream();
                date1 = params[0].y;
                date2 = params[0].z;

                CalendarBuilder builder = new CalendarBuilder();
                calendar = builder.build(fileCal);
            } catch (Exception e) {
                calendar = new Calendar();
                date1 = "error";            //c'est immonde mais ça marche (you'll see what I mean soon)
                date2 = "error";

            }
            return new Tuple<>(calendar, date1, date2);
        }

        @Override
        protected void onPostExecute(Tuple<Calendar, String, String> entreeOnPost) {
            Main2Activity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            Calendar calendar = entreeOnPost.x;
            String date1 = entreeOnPost.y;
            String date2 = entreeOnPost.z;

            //Avant de critiquer essaie de trouver une meilleur solution (la propagation des exeptions entre ces 2 methodes est compliqué à mettre en place)
            if (date1.equals("error")) {
                Intent toError = new Intent(activity, Error.class);
                toError.putExtra("messageErreur", "Echec du télécharchement du Calendrier\nedt.inp-toulouse.fr est peut-être down");
                activity.startActivity(toError);
            }

            //manual parsing
            String t1 = date1.substring(0, 4) + date1.substring(5, 7) + date1.substring(8) + "00";
            String t2 = date2.substring(0, 4) + date2.substring(5, 7) + date2.substring(8) + "00";


            try {
                DateTime h1 = new DateTime(t1);
                DateTime h2 = new DateTime(t2);
                PeriodRule rule = new PeriodRule(new Period(h1, h2));

                Resources res = activity.getResources(); //C'est tortueux de recourir à des ressources mais ça sera plus propre si on étend le principe à d'autre type de salles (td...)
                List<String> listSalles = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.sallesInfo)));
                for (Component component : calendar.getComponents()) {
                    try {
                        String lieu = component.getProperty("LOCATION").getValue();

                        if (rule.test(component)) {         // aka if the event have an occurrence between h1 and h2
                            for (String l : lieu.split(",")) {  //aka for each location mentioned in the event
                                listSalles.remove(l);
                            }
                        }

                    } catch (Exception e) {
                        //Yolo bis (ugly but controlled)
                    }
                }

                gestionDoublonSalle(listSalles, "C214");
                gestionDoublonSalle(listSalles, "C216");

                StringBuilder textSalles = new StringBuilder();

                if (listSalles.isEmpty()) {
                    textSalles = new StringBuilder("Aucune salle libre");
                } else {

                    for (String salle : listSalles) {
                        textSalles.append(salle).append("\n");
                    }

                }

                //Affichage
                TextView sallesLibres = activity.findViewById(R.id.sallesLibres);
                sallesLibres.setText(textSalles.toString());

            } catch (ParseException e) {
                Intent toError = new Intent(activity, Error.class);
                toError.putExtra("messageErreur", "Echec de l'analyse syntaxique du temps");
                activity.startActivity(toError);
            }
            }
        }

    /**Gestion des salles qui ont 2 noms comme la C214 (C214a et C214b)*/
    public static void gestionDoublonSalle(List<String> listSalles, String salleDouble){

        boolean salleDoubleaEstDansListSalles = listSalles.contains(salleDouble + "a");
        boolean salleDoublebEstDansListSalles = listSalles.contains(salleDouble + "b");

        if (salleDoubleaEstDansListSalles && salleDoublebEstDansListSalles) {
            listSalles.remove(salleDouble + "a");
            listSalles.remove(salleDouble + "b");
            listSalles.add(salleDouble);
        } else if (salleDoubleaEstDansListSalles || salleDoublebEstDansListSalles) {
            listSalles.remove(salleDouble + "a");
            listSalles.remove(salleDouble + "b");
        }
    }
}

