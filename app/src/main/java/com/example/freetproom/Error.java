package com.example.freetproom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Error extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Intent toError = getIntent();
        String messageErreur = toError.getStringExtra("messageErreur");
        TextView errorTextView = findViewById(R.id.errorTextView);
        errorTextView.setText(messageErreur);
    }

    /**Retourne à l'acceuil*/
    public void retry(View view){
        Intent toAcceuil = new Intent(this, Accueil.class);
        startActivity(toAcceuil);
    }

}
