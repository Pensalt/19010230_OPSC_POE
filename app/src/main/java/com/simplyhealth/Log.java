package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Log extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    TextView headingTV, weightOnDayTV, goalWeightTV, caloriesConsumedTV, dailyCalorieGoalTV, weightOnDayNumTV, caloriesOnDayNumTV
            , goalWeightNumTV, goalCaloriesNumTV;
    CalendarView historyCalendarCV;
    Button recordDailyWeightBtn, captureMealBtn;
    ImageButton settingsImgBtn;
    BarChart calChart, weightChart;
    ArrayList<BarEntry> calorieEntry, weightEntry;
    BarDataSet calorieSet, weightSet;
    BarData calData, weightData;

    Boolean useMetric;
    User u;
    static DailyWeightInfo d = new DailyWeightInfo();
    MealBreakdown m = new MealBreakdown();
    double goalCal, goalWeight;

    //ArrayList CalxAxis;
    //public static void d(String tag, String key) { } // remove


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        mAuth = FirebaseAuth.getInstance();
        final FirebaseDatabase db = FirebaseDatabase.getInstance();


        headingTV = findViewById(R.id.simplyHealthTV_log);
        weightOnDayTV = findViewById(R.id.weightOnDayTV_log);
        weightOnDayNumTV = findViewById(R.id.weightNumTV_log);
        goalWeightTV = findViewById(R.id.weightGoalTV_log);
        goalWeightNumTV = findViewById(R.id.weightGoalNumTV_log);
        dailyCalorieGoalTV = findViewById(R.id.calGoalTV_log);
        caloriesConsumedTV = findViewById(R.id.calOnDayTV_log);
        caloriesOnDayNumTV = findViewById(R.id.calNumTV_log);
        goalCaloriesNumTV = findViewById(R.id.calGoalNumTV_log);
        historyCalendarCV = findViewById(R.id.historyCalenderView_log);
        recordDailyWeightBtn = findViewById(R.id.recordDailyWeightButton_log);
        captureMealBtn = findViewById(R.id.captureMealButton_log);
        settingsImgBtn = findViewById(R.id.settingsImgBtn_log);
        calChart = findViewById(R.id.calChart_log);


        settingsImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toSettings = new Intent(Log.this, SettingsActivity.class);
                startActivity(toSettings);
            }
        });

        captureMealBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent toCaptureMeal = new Intent(Log.this, CaptureMeals.class);
                startActivity(toCaptureMeal);
            }
        });


        recordDailyWeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRecordWeight = new Intent(Log.this, RecordWeight.class);
                startActivity(toRecordWeight);
            }
        });

        InitialDataPopulation();



        historyCalendarCV.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                try {

                    calorieEntry = new ArrayList<>(); // Array list for the current and goal calorie amounts.
                    calorieSet = new BarDataSet(calorieEntry,"");
                    calorieEntry.add(new BarEntry((float) u.getGoalCalories(),1));
                    final String selectedDate = i2 + "-" + (i1 + 1) + "-" + i; // Getting the currently selected date and formatting as needed (dd-MM-yyyy). The plus 1 is necessary because Jan is 0 instead of 1

                    DatabaseReference ref = db.getReference(mAuth.getCurrentUser().getUid());
                    ref.child("Daily Weight").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //long selected = historyCalendarCV.getDate();
                            //SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                            //String currentDate = df.format(selected);


                            Boolean found = false;
                            for (DataSnapshot daily : snapshot.getChildren()){

                                d = daily.getValue(DailyWeightInfo.class);

                                if (d.getCaptureDate().equals(selectedDate)){

                                    if (u.getUseMetric() == false){
                                        double currentW = d.getWeight() * 2.205;
                                        weightOnDayNumTV.setText(currentW + "");
                                        //found = true;
                                        //break;
                                    }
                                    else
                                    {
                                        weightOnDayNumTV.setText(d.getWeight() + "");
                                        //found = true;
                                    }
                                    found = true;
                                    //break;
                                }
                            }

                            if (found == false){
                                weightOnDayNumTV.setText("NA");
                                //caloriesOnDayNumTV.setText("NA");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    ref.child("Meal Info").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //Long selected = historyCalendarCV.getDate();
                            //SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                            //String selectedDate = df.format(selected);
                            double dailyTotalCal = 0;

                            Boolean found = false;

                            for (DataSnapshot daily : snapshot.getChildren()){

                                m = daily.getValue(MealBreakdown.class);

                                if (m.getCaptureDate().equals(selectedDate)){

                                    dailyTotalCal += m.getCalories();
                                    found = true;
                                }
                            }


                            if (found == false){
                                caloriesOnDayNumTV.setText("NA");
                                calorieEntry.add(new BarEntry(0,0));
                            }
                            else
                            {
                                caloriesOnDayNumTV.setText(dailyTotalCal +"");
                                calorieEntry.add(new BarEntry((float) dailyTotalCal,0));
                            }

                            BarData data = new BarData(getCalXAxisValues(), calorieSet);
                            calChart.setData(data);
                            calChart.setDescription("My Chart");
                            calChart.animateXY(2000, 2000);
                            calChart.invalidate();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e){
                    Toast.makeText(Log.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    // Method to populate the data for the day on load of the form.
    public void InitialDataPopulation(){

        calorieEntry = new ArrayList<>(); // Array list for the current and goal calorie amounts.
        calorieSet = new BarDataSet(calorieEntry,"");


        try {
            DatabaseReference ref = db.getReference(mAuth.getCurrentUser().getUid());
            ref.child("User Details").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    u = snapshot.getValue(User.class);

                    if (u.getUseMetric() == false){
                        useMetric = false;
                        weightOnDayTV.setText(R.string.weight_imp);
                        goalWeightTV.setText(R.string.goal_weight_imp);

                        double impWeightGoal = u.getGoalWeight() * 2.205;
                        //goalCaloriesNumTV.setText(u.getGoalCalories() + "");
                        goalWeightNumTV.setText(impWeightGoal + "");

                    } else {
                        useMetric = true;
                        goalWeightTV.setText(R.string.goal_weight_met);
                        goalWeightNumTV.setText(u.getGoalWeight() +"");
                        //goalCaloriesNumTV.setText(u.getGoalCalories()+"");
                        weightOnDayTV.setText(R.string.weight_met);
                        goalWeight = u.getGoalWeight();
                    }

                    goalCaloriesNumTV.setText(u.getGoalCalories() + ""); // Showing the user's goal calories

                    calorieEntry.add(new BarEntry((float) u.getGoalCalories(),1));

                    //calorieEntry.add(new BarEntry(1, (float) u.getGoalCalories()));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });

            ref.child("Daily Weight").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    Date current = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                    String currentDate = df.format(current);
                    Boolean found = false;

                    for (DataSnapshot daily : snapshot.getChildren()){

                        d = daily.getValue(DailyWeightInfo.class);

                        if (d.getCaptureDate().equals(currentDate)){
                            //u.setCurrentWeight(d.getWeight()); // Updating the user's current weight.

                            //ref.child("User Details").child("currentWeight").
                            if (useMetric == false){
                                double currentW = d.getWeight() * 2.205;
                                weightOnDayNumTV.setText(currentW + "");
                            }
                            else
                            {
                                weightOnDayNumTV.setText(Math.round(d.getWeight()) + "");
                            }
                            found = true;
                            break;
                        }
                    }

                    if (found == false){
                        weightOnDayNumTV.setText("NA");
                        //caloriesOnDayNumTV.setText("NA");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });

            ref.child("Meal Info").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    Date current = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
                    String currentDate = df.format(current);
                    double dailyTotalCal = 0;

                    Boolean found = false;
                    for (DataSnapshot daily : snapshot.getChildren()){

                        m = daily.getValue(MealBreakdown.class);

                        if (m.getCaptureDate().equals(currentDate)){

                            dailyTotalCal += m.getCalories();
                            found = true;
                        }
                    }


                    if (found == false){
                        caloriesOnDayNumTV.setText("NA");
                        calorieEntry.add(new BarEntry(0,0));

                    }
                    else
                    {
                        caloriesOnDayNumTV.setText(dailyTotalCal +"");
                        calorieEntry.add(new BarEntry((float) dailyTotalCal,0));
                        //calorieEntry.add(new BarEntry(0, (float) dailyTotalCal));

                    }

                    BarData data = new BarData(getCalXAxisValues(), calorieSet);
                    calChart.setData(data);
                    calChart.setDescription("My Chart");
                    calChart.animateXY(2000, 2000);
                    calChart.invalidate();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });



        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //BarData data = new BarData(getCalXAxisValues(), calorieSet);
        //calChart.setData(data);
        //calChart.setDescription("My Chart");
        //calChart.animateXY(2000, 2000);
        //calChart.invalidate();
        //calorieSet = new BarDataSet(calorieEntry,"Calories");
        //ArrayList<IBarDataSet> set = new ArrayList<>();
        //set.add(calorieSet);
        //calData = new BarData(set);
        //calorieChart.setData(calData);
    }



    private ArrayList getCalXAxisValues() {
        ArrayList CalxAxis = new ArrayList();
        CalxAxis.add("Current");
        CalxAxis.add("Goal");
        return CalxAxis;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void Resume() {
        //super.onResume();
        //weightOnDayNumTV.setText("");
        InitialDataPopulation();
    }
}