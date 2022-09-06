package com.xar.lore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xar.lore.groups.DemocracyActivity;
import com.xar.lore.groups.ElectocracyActivity;
import com.xar.lore.groups.MonarchyActivity;
import com.xar.lore.groups.RepublicActivity;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_setting);

        // Find the View that shows the electocracy category
        TextView electocracy = findViewById(R.id.electocracy);

        // Set a click listener on that View
        electocracy.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the electocracy category is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link ElectocracyActivity}
                Intent numbersIntent = new Intent(SettingActivity.this, ElectocracyActivity.class);

                // Start the new activity
                startActivity(numbersIntent);
            }
        });

        // Find the View that shows the monarchy category
        TextView family = findViewById(R.id.monarchy);

        // Set a click listener on that View
        // The code in this method will be executed when the monarchy category is clicked on.
        family.setOnClickListener(view -> {
            // Create a new intent to open the {@link MonarchyActivity}
            Intent familyIntent = new Intent(SettingActivity.this, MonarchyActivity.class);

            // Start the new activity
            startActivity(familyIntent);
        });

        // Find the View that shows the democracy category
        TextView democracy = findViewById(R.id.democracy);

        // Set a click listener on that View
        democracy.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the democracy category is clicked on.
            @Override
            public void onClick(View view) {
                // Create a new intent to open the {@link DemocracyActivity}
                Intent colorsIntent = new Intent(SettingActivity.this, DemocracyActivity.class);

                // Start the new activity
                startActivity(colorsIntent);
            }
        });

        // Find the View that shows the republic category
        TextView republic = findViewById(R.id.republic);

        // Set a click listener on that View
        // The code in this method will be executed when the republic category is clicked on.
        republic.setOnClickListener(view -> {
            // Create a new intent to open the {@link RepublicActivity}
            Intent phrasesIntent = new Intent(SettingActivity.this, RepublicActivity.class);

            // Start the new activity
            startActivity(phrasesIntent);
        });
    }
}
