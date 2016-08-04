package be.buri.battleships.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Arrays;

import be.buri.battleships.R;
import be.buri.battleships.Units.Harbor;

public class HarborListActivity extends AppCompatActivity {

    private RadioGroup harborsRadioGroup;
    public ArrayList<Harbor> harborsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harbor_list);
        // get the radio group of harbors
        harborsRadioGroup = (RadioGroup) findViewById(R.id.harborsRadioGroup);

        // hide the Next button
        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setVisibility(View.GONE);

        // initialize the harbors array
        harborsArray = new ArrayList<Harbor>() {
            {
                add(new Harbor("Aarhus", 56.156998, 10.213376));
                add(new Harbor("Aalborg", 57.053255, 9.916739));
                add(new Harbor("Copenhagen", 55.676087, 12.587616));
                add(new Harbor("Esbjerg", 55.463119, 8.440151));
                add(new Harbor("Frederikshavn", 57.439275, 10.544074));
                add(new Harbor("Hanstholm", 57.120962, 8.598468));
                add(new Harbor("Hirtshals", 57.594133, 9.969568));
                add(new Harbor("Grenaa", 56.408487, 10.922023));
                add(new Harbor("Skagen", 57.718120, 10.588968));
            }
        };
        // make harbors as a radio button
        for (Harbor item : harborsArray) {
            makeRadioButton(item.getName());
        }

        //ClientService service = (ClientService);
    }

    public void launchPlayerList(View view)
    {
        // TODO save the player name and the selected harbor
        Intent showPlayerList = new Intent(this, PlayerListActivity.class);
        startActivity(showPlayerList);
    }

    private void makeRadioButton(String name)
    {
        RadioButton newRadioButton = new RadioButton(this);
        newRadioButton.setText(name);
        newRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show the next button
                Button nextButton = (Button) findViewById(R.id.nextButton);
                nextButton.setVisibility(View.VISIBLE);
            }
        });
        harborsRadioGroup.addView(newRadioButton);
    }
}
