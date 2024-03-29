 package com.simplyhealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    TextView headingTV, weightOnDayTV, goalWeightTV, caloriesConsumedTV, dailyCalorieGoalTV, weightOnDayNumTV, caloriesOnDayNumTV, goalWeightNumTV, goalCaloriesNumTV, calGraphHeadingTV, weightGraphHeadingTV;
    CalendarView historyCalendarCV;
    Button recordDailyWeightBtn, captureMealBtn;
    ImageButton settingsImgBtn;
    BarChart calChart, weightChart;

    Boolean useMetric;
    User u;
    static DailyWeightInfo d = new DailyWeightInfo();
    MealBreakdown m = new MealBreakdown();
    double goalCal, goalWeight, currentCal, currentWeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

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
        weightChart = findViewById(R.id.weightChart_log);
        calGraphHeadingTV = findViewById(R.id.calGraphHeading_log);
        weightGraphHeadingTV = findViewById(R.id.weightGraphHeading_log);


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

        historyCalendarCV.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                try {

                    // Initialising current calories to 0 and goal calories to the user's goal calories.
                    currentCal = 0;

                    // Initialising current weight to 0 and goal weight to the user's goal weight.
                    currentWeight = 0;

                    // The following line of code is an adaptation from
                    // Available at: https://www.youtube.com/watch?v=hHjFIG0TtA0&t=331s
                    // Author: CodingWithMitch
                    final String selectedDate = i2 + "-" + (i1 + 1) + "-" + i; // Getting the currently selected date and formatting as needed (dd-MM-yyyy). The plus 1 is necessary because Jan is 0 instead of 1

                    DatabaseReference ref = db.getReference(mAuth.getCurrentUser().getUid());
                    ref.child("Daily Weight").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            Boolean found = false; // Flag for if the user has information on the specified day.
                            for (DataSnapshot daily : snapshot.getChildren()) {

                                d = daily.getValue(DailyWeightInfo.class);

                                if (d.getCaptureDate().equals(selectedDate)) {

                                    if (!u.getUseMetric()) {
                                        double currentW = d.getWeight() * 2.205;
                                        weightOnDayNumTV.setText(currentW + "");
                                        currentWeight = currentW; // Setting the user's current weight value to the imperial value for current weight.
                                    } else {
                                        weightOnDayNumTV.setText(d.getWeight() + "");
                                        currentWeight = d.getWeight(); // Setting the user's current weight value to the weight value from the current day.
                                    }
                                    found = true;
                                    //break;
                                }
                            }

                            if (!found) {
                                weightOnDayNumTV.setText("NA");
                                currentWeight = 0;
                            }

                            generateWeightGraph(currentWeight, goalWeight); // Calling the method to generate the weight bar graph.
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    ref.child("Meal Info").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            double dailyTotalCal = 0; // Initialising the current day's total calorie count to 0.

                            Boolean found = false;

                            // Iterating through all the captured meals.
                            for (DataSnapshot daily : snapshot.getChildren()) {

                                m = daily.getValue(MealBreakdown.class);

                                // Only meals for the selected date are shown
                                if (m.getCaptureDate().equals(selectedDate)) {

                                    dailyTotalCal += m.getCalories(); // Summing the calories for the selected day.
                                    found = true;
                                }
                            }


                            if (!found) {
                                caloriesOnDayNumTV.setText("NA");
                                currentCal = 0; // Setting current calories to 0 if the user has not yet recorded calories for the day. // might not be necessary
                            } else {

                                caloriesOnDayNumTV.setText(dailyTotalCal + "");
                                currentCal = dailyTotalCal; // Setting current calories to the daily total calorie count.
                            }
                            generateCalorieGraph(currentCal, goalCal); // Calling the method to generate the calorie graph.
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(Log.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        InitialDataPopulation(); // Runs the method which populates data on load of the activity.

    }

    // Method to populate the data for the day on load of the form.
    public void InitialDataPopulation() {
        // Initialising current and goal calories to 0.
        currentCal = 0;
        goalCal = 0;

        // Initialising current and goal weight to 0.
        currentWeight = 0;
        goalWeight = 0;


        try {
            DatabaseReference ref = db.getReference(mAuth.getCurrentUser().getUid());
            ref.child("User Details").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    u = snapshot.getValue(User.class);

                    if (!u.getUseMetric()) {
                        useMetric = false;
                        weightOnDayTV.setText(R.string.weight_imp);
                        goalWeightTV.setText(R.string.goal_weight_imp);

                        double impWeightGoal = u.getGoalWeight() * 2.205;
                        goalWeightNumTV.setText(impWeightGoal + "");
                        goalWeight = impWeightGoal; // Setting imperial goal weight based on user's goal weight

                    } else {
                        useMetric = true;
                        goalWeightTV.setText(R.string.goal_weight_met);
                        goalWeightNumTV.setText(u.getGoalWeight() + "");
                        weightOnDayTV.setText(R.string.weight_met);
                        goalWeight = u.getGoalWeight(); // Setting metric goal weight based on user's goal weight
                    }

                    goalCaloriesNumTV.setText(u.getGoalCalories() + ""); // Showing the user's goal calories

                    goalCal = u.getGoalCalories(); // Setting goal calories based on the user's goal calories
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

                    for (DataSnapshot daily : snapshot.getChildren()) {

                        d = daily.getValue(DailyWeightInfo.class);

                        if (d.getCaptureDate().equals(currentDate)) {

                            if (!useMetric) {
                                double currentW = d.getWeight() * 2.205;
                                weightOnDayNumTV.setText(currentW + "");
                                currentWeight = currentW; // Setting the user's current weight value to the imperial value for current weight.
                            } else {
                                weightOnDayNumTV.setText(d.getWeight() + "");
                                currentWeight = d.getWeight(); // Setting the user's current weight value to the weight value from the current day.
                            }
                            found = true;
                            //break;
                        }
                    }

                    if (!found) {
                        weightOnDayNumTV.setText("NA");
                        currentWeight = 0;
                    }

                    generateWeightGraph(currentWeight, goalWeight); // Calling the method to generate the weight graph.
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
                    for (DataSnapshot daily : snapshot.getChildren()) {

                        m = daily.getValue(MealBreakdown.class);

                        if (m.getCaptureDate().equals(currentDate)) {

                            dailyTotalCal += m.getCalories();
                            found = true;
                        }
                    }


                    if (!found) {
                        caloriesOnDayNumTV.setText("NA");
                        currentCal = 0; // Setting current calories to 0 if the user has not yet recorded calories for the day. // might not be necessary
                    } else {
                        caloriesOnDayNumTV.setText(dailyTotalCal + "");
                        currentCal = dailyTotalCal; // Setting current calories to the daily total calorie count.
                    }

                    generateCalorieGraph(currentCal, goalCal); // Calling the method to generate the calorie graph.
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(Log.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });


        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // The following code is adapted from
    // Available at: https://medium.com/@mobindustry/how-to-quickly-implement-beautiful-charts-in-your-android-app-cf4caf050772
    // Author: Mobindustry
    private ArrayList getXAxisValues() {
        ArrayList xAxis = new ArrayList();
        xAxis.add("Current");
        xAxis.add("Goal");
        return xAxis;
    }

    // Method to handle the generation of the calorie bar graph.
    public void generateCalorieGraph(double currCal, double gCal) {

        // The following code is adapted from
        // Available at: https://medium.com/@mobindustry/how-to-quickly-implement-beautiful-charts-in-your-android-app-cf4caf050772
        // Author: Mobindustry

        // Declaring BarEntry for current and goal calories.
        BarEntry curr = new BarEntry((float) currCal, 0);
        BarEntry goal = new BarEntry((float) gCal, 1);

        // Declaring array list of BarEntries and adding the current and goal BarEntries.
        ArrayList<BarEntry> calorieEntries = new ArrayList<>();
        calorieEntries.add(curr);
        calorieEntries.add(goal);

        // Declaring BarDataSet and defining its colours
        BarDataSet calorieSet = new BarDataSet(calorieEntries, "Calories");
        calorieSet.setColor(Color.rgb(154, 202, 60)); // Setting the colour of the set to the green accent used throughout the app.

        // Declaring BarData
        BarData calData = new BarData(getXAxisValues(), calorieSet);

        // Initialising the bar graph.
        calChart.setData(calData);

        // The following code is adapted from
        // Available at: https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/BarChartActivity.java
        // Author: PhilJay

        // Formatting the X Axis position to the bottom.
        XAxis x = calChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Setting the axis to start at 0 causes issues when the values for current and goal are similar so it has been commented out.
        // This is unfortunate as it is more visually appealing when the graph starts at 0
        /*// Formatting the left and right Y Axis to start at 0.
        YAxis rightAxis = calChart.getAxisRight();
        rightAxis.setAxisMinValue(0f);
        YAxis leftAxis = calChart.getAxisLeft();
        leftAxis.setAxisMinValue(0f)*/;

        calChart.setDescription("");
        calChart.animateXY(2000, 2000);
        calChart.invalidate();

    }

    // Method to handle the generation of the weight bar graph.
    public void generateWeightGraph(double currWeight, double gWeight) {

        // The following code is adapted from
        // Available at: https://medium.com/@mobindustry/how-to-quickly-implement-beautiful-charts-in-your-android-app-cf4caf050772
        // Author: Mobindustry

        // Declaring BarEntry for current and goal weight.
        BarEntry curr = new BarEntry((float) currWeight, 0);
        BarEntry goal = new BarEntry((float) gWeight, 1);

        // Declaring array list of BarEntries and adding the current and goal BarEntries.
        ArrayList<BarEntry> weightEntries = new ArrayList<>();
        weightEntries.add(curr);
        weightEntries.add(goal);

        // Declaring BarDataSet and defining its   colours
        BarDataSet weightSet = new BarDataSet(weightEntries, "Weight");
        weightSet.setColor(Color.rgb(154, 202, 60)); // Setting the colour of the set to the green accent used throughout the app.

        // Declaring BarData
        BarData weightData = new BarData(getXAxisValues(), weightSet);

        // Initialising the bar graph.
        weightChart.setData(weightData);

        // The following code is adapted from
        // Available at: https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/BarChartActivity.java
        // Author: PhilJay

        // Formatting the X Axis position to the bottom.
        XAxis x = weightChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Setting the axis to start at 0 causes issues when the values for current and goal are similar so it has been commented out.
        // This is unfortunate as it is more visually appealing when the graph starts at 0
        /*// Formatting the left and right Y Axis to start at 0.
        YAxis rightAxis = weightChart.getAxisRight();
        rightAxis.setAxisMinValue(0f);
        YAxis leftAxis = weightChart.getAxisLeft();
        leftAxis.setAxisMinValue(0f);*/

        weightChart.setDescription("");
        weightChart.animateXY(2000, 2000);
        weightChart.invalidate();
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void Resume() {
        InitialDataPopulation();
    }
}