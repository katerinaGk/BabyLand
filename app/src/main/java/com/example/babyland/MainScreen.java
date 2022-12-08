package com.example.babyland;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class MainScreen extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference reference;
    Boolean userFound = false;
    private ArrayList<Baby> listKids;
    String currentUser;
    private RelativeLayout noBabyLayout, mainScreenLayout, statisticLayout;
    private ImageButton addBabyButton;
    private Spinner chooseChildSpinner;
    private Button addDevelopmentButton, showDevelopmentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);


        //getting views
        noBabyLayout = findViewById(R.id.noBabyLayout);
        addBabyButton = findViewById(R.id.addBabyButton);
        mainScreenLayout = findViewById(R.id.mainScreenLayout);
        chooseChildSpinner = findViewById(R.id.chooseBabySpinner);
        statisticLayout = findViewById(R.id.statisticLayout);
        addDevelopmentButton = findViewById(R.id.addDevelopmentButton);
        showDevelopmentButton = findViewById(R.id.showDevelopmentButton);

        listKids=null;
        userFound = false;

        //getting visibilities on xml file
        noBabyLayout.setVisibility(View.INVISIBLE);
        mainScreenLayout.setVisibility(View.VISIBLE);

        //setting database
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //getting user info from database parent
        reference = database.getReference("parent");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot!=null){
                    for(DataSnapshot snapshots : snapshot.getChildren()){
                        String UID = snapshots.getKey();
                        if (UID.equals(currentUser)) {
                            userFound = true;
                            GenericTypeIndicator<ArrayList<Baby>> t = new GenericTypeIndicator<ArrayList<Baby>>(){};
                            listKids = snapshots.child("kids").getValue(t);
                        }
                    }
                }
                load();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        addBabyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddBaby.class);
                startActivity(intent);
            }
        });

        addDevelopmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String babyAmka = chooseChildSpinner.getSelectedItem().toString();
                String date = " ";

                for (int i = 0; i < listKids.size(); i++) {
                    if (listKids.get(i).getAmka() == babyAmka) {
                        date = listKids.get(i).getDateOfBirth();
                    }
                }
                Calendar cal = Calendar.getInstance();
                int yearNow = cal.get(Calendar.YEAR);
                int monthNow = cal.get(Calendar.MONTH) + 1;
                int dayNow = cal.get(Calendar.DAY_OF_MONTH);
                int i = 0;
                String day = "", month = "", year = "";
                for (Character c : date.toCharArray()) {
                    if (i < 2) {
                        day = day + c;
                    } else if (i > 2 && i < 5) {
                        month = month + c;
                    } else if (i > 5 && i < 11) {
                        year = year + c;
                    }
                    i++;
                }
                int yearOfBirth = Integer.parseInt(year);
                int monthOfBirth = Integer.parseInt(month);
                int dayOfBirth = Integer.parseInt(day);
                int monthsD;
                if (yearOfBirth == yearNow && monthOfBirth == monthNow) {
                    if (dayNow - dayOfBirth <= 14) {
                        monthsD = 0;
                    } else {
                        monthsD = 1;
                    }
                } else if (yearOfBirth == yearNow) {
                    monthsD = monthNow - monthOfBirth;
                } else {
                    int m = 12 - monthOfBirth;
                    int m1 = yearNow - (yearOfBirth + 1);
                    m1 = m1 * 12;
                    monthsD = m + m1 + monthNow;
                }
                String age = "";
                String ageType = "";
                if (monthsD == 0) {
                    ageType = "weeks";
                    age = "1-2";
                } else if (monthsD <= 2) {
                    ageType = "months";
                    age = "2";
                } else if (monthsD == 3 || monthsD == 4) {
                    ageType = "months";
                    age = String.valueOf(monthsD);
                } else if (monthsD == 5 || monthsD == 6) {
                    ageType = "months";
                    age = String.valueOf(monthsD);
                } else if (monthsD >= 7 && monthsD <= 9) {
                    ageType = "months";
                    age = String.valueOf(monthsD);
                } else if (monthsD >= 10 && monthsD <= 15) {
                    ageType = "months";
                    age = String.valueOf(monthsD);
                }

                Intent intent = new Intent(MainScreen.this, MonitoringDevelopment.class);
                intent.putExtra("babyAmka", babyAmka);
                intent.putExtra("ageType", ageType);
                intent.putExtra("age", age);
                startActivity(intent);
            }
        });

    }


    public void load(){
        if(listKids != null && !listKids.isEmpty()){
            //show panel with babies
            noBabyLayout.setVisibility(View.INVISIBLE);
            mainScreenLayout.setVisibility(View.VISIBLE);
            showKids();
        }else{
            if(userFound){
                //user found with none baby
                //show panel no babies
                mainScreenLayout.setVisibility(View.INVISIBLE);
                noBabyLayout.setVisibility(View.VISIBLE);
            }else{
                //user not found
                //create new user
                Intent intent = new Intent(getApplicationContext(), createNewUser.class);
                startActivity(intent);
            }
        }
    }

    public void showKids() {
        ArrayAdapter<Baby> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listKids);
        chooseChildSpinner.setAdapter(adapter);

        chooseChildSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
              //  showMessage("title", chooseChildSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    //button to show development
    public void showDevelopment(View view){
        Intent intent = new Intent(this, showDevelopmentsList.class);
        intent.putExtra("babyAmka", chooseChildSpinner.getSelectedItem().toString());
        startActivity(intent);
    }





    //showing messages to users
    public void showMessage(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}