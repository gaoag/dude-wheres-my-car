package com.example.parkour;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class FilterZoneRowView extends LinearLayout {


    ImageView alarmIcon;
    Button noneButton;
    Button twoHrButton;
    Button fourHrButton;
    Button sixHrButton;

    public FilterZoneRowView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.multi_button_row_view, this);

        initComponents();
    }

    private void initComponents() {

        alarmIcon = (ImageView) findViewById(R.id.alarmImage);

        noneButton = (Button) findViewById(R.id.none);
        noneButton.setTag(0);
        twoHrButton = (Button) findViewById(R.id.twohr);
        twoHrButton.setTag(2);
        fourHrButton = (Button) findViewById(R.id.fourhr);
        fourHrButton.setTag(4);
        sixHrButton = (Button) findViewById(R.id.sixhr);
        sixHrButton.setTag(6);

        styleAllButtonsExcept(0);
    }

    public void styleAllButtonsExcept(int buttonPressed) {
        Button toEdit = noneButton;
        if (buttonPressed == 0) {
            toEdit = noneButton;
        } else if (buttonPressed == 2) {
            toEdit = twoHrButton;
        } else if (buttonPressed == 4) {
            toEdit = fourHrButton;
        } else if (buttonPressed == 6) {
            toEdit = sixHrButton;
        }

        noneButton.setTextColor(Color.argb(95, 255, 255, 255));
        noneButton.setBackgroundColor(Color.rgb(47,128,237));
        twoHrButton.setTextColor(Color.argb(95, 255, 255, 255));
        twoHrButton.setBackgroundColor(Color.rgb(47,128,237));
        fourHrButton.setTextColor(Color.argb(95, 255, 255, 255));
        fourHrButton.setBackgroundColor(Color.rgb(47,128,237));
        sixHrButton.setTextColor(Color.argb(95, 255, 255, 255));
        sixHrButton.setBackgroundColor(Color.rgb(47,128,237));

        toEdit.setTextColor(Color.WHITE);
        toEdit.setBackgroundResource(R.drawable.filter_button_background);


    }
}
