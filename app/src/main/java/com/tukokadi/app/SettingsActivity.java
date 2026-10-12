package com.tukokadi.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch soundSwitch = findViewById(R.id.soundSwitch);
        Switch vibrationSwitch = findViewById(R.id.vibrationSwitch);
        Switch autoMatchSwitch = findViewById(R.id.autoMatchSwitch);
        RadioGroup stakeGroup = findViewById(R.id.stakeGroup);
        Button backButton = findViewById(R.id.backButton);

        soundSwitch.setChecked(Prefs.isSoundEnabled(this));
        vibrationSwitch.setChecked(Prefs.isVibrationEnabled(this));
        autoMatchSwitch.setChecked(Prefs.isAutoMatch(this));

        switch (Prefs.getPreferredStake(this)) {
            case 200: ((RadioButton) findViewById(R.id.stake200)).setChecked(true); break;
            case 500: ((RadioButton) findViewById(R.id.stake500)).setChecked(true); break;
            case 1000: ((RadioButton) findViewById(R.id.stake1000)).setChecked(true); break;
            default: ((RadioButton) findViewById(R.id.stake100)).setChecked(true); break;
        }

        soundSwitch.setOnCheckedChangeListener(
                (v, checked) -> Prefs.setSoundEnabled(this, checked));
        vibrationSwitch.setOnCheckedChangeListener(
                (v, checked) -> Prefs.setVibrationEnabled(this, checked));
        autoMatchSwitch.setOnCheckedChangeListener(
                (v, checked) -> Prefs.setAutoMatch(this, checked));

        stakeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.stake100) Prefs.setPreferredStake(this, 100);
            else if (checkedId == R.id.stake200) Prefs.setPreferredStake(this, 200);
            else if (checkedId == R.id.stake500) Prefs.setPreferredStake(this, 500);
            else if (checkedId == R.id.stake1000) Prefs.setPreferredStake(this, 1000);
        });

        backButton.setOnClickListener(v -> finish());
    }
}
