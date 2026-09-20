package com.tukokadi.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class AuthActivity extends Activity {

    private static final Pattern SAFARICOM_PATTERN = Pattern.compile(
        "^(?:\\+254|254|0)(7(0[0-8]|1[0-2]|2[0-9]|5[7-9]|6[8-9]|9[0-9])|11[0-5])\\d{6}$"
    );

    private LinearLayout loginSection, registerSection;
    private EditText loginPhone, loginPassword;
    private EditText registerName, registerPhone, registerPassword, registerConfirmPassword;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        prefs = getSharedPreferences("tuko_kadi_users", MODE_PRIVATE);

        loginSection = findViewById(R.id.loginSection);
        registerSection = findViewById(R.id.registerSection);

        loginPhone = findViewById(R.id.loginPhone);
        loginPassword = findViewById(R.id.loginPassword);

        registerName = findViewById(R.id.registerName);
        registerPhone = findViewById(R.id.registerPhone);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfirmPassword = findViewById(R.id.registerConfirmPassword);

        Button loginButton = findViewById(R.id.loginButton);
        Button forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        Button showRegisterButton = findViewById(R.id.showRegisterButton);
        Button registerButton = findViewById(R.id.registerButton);
        TextView contactSupportText = findViewById(R.id.contactSupportText);

        loginButton.setOnClickListener(v -> serverLogin());
        registerButton.setOnClickListener(v -> serverRegister());

        showRegisterButton.setOnClickListener(v -> {
            boolean showingRegister = registerSection.getVisibility() == View.VISIBLE;
            registerSection.setVisibility(showingRegister ? View.GONE : View.VISIBLE);
            showRegisterButton.setText(showingRegister ? "New here? Register" : "Already have an account? Login");
        });

        forgotPasswordButton.setOnClickListener(v ->
            Toast.makeText(this, "Forgot password flow coming soon", Toast.LENGTH_SHORT).show()
        );

        contactSupportText.setOnClickListener(v ->
            Toast.makeText(this, "Support contact coming soon", Toast.LENGTH_SHORT).show()
        );
    }

    private String normalizePhone(String rawPhone) {
        String digitsOnly = rawPhone.replaceAll("\\s+", "");
        if (digitsOnly.startsWith("+254")) {
            digitsOnly = "0" + digitsOnly.substring(4);
        } else if (digitsOnly.startsWith("254")) {
            digitsOnly = "0" + digitsOnly.substring(3);
        }
        return digitsOnly;
    }

    private boolean isSafaricomNumber(String phone) {
        return SAFARICOM_PATTERN.matcher(phone).matches();
    }

    private void serverLogin() {
        final String phone = normalizePhone(loginPhone.getText().toString().trim());
        String password = loginPassword.getText().toString();
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter phone number and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isSafaricomNumber(phone)) {
            Toast.makeText(this, "Enter a valid Safaricom number", Toast.LENGTH_SHORT).show();
            return;
        }

        // TEMP BYPASS: server not reachable yet - skip network call and log in locally.
        Api.saveSession(this, "dev-local-token", phone);
        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
        finish();

        /*
        try {
            org.json.JSONObject body = new org.json.JSONObject();
            body.put("phone", phone);
            body.put("password", password);
            Api.post("/login", body, null, (ok, json, error) -> {
                if (!ok) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    return;
                }
                Api.saveSession(this, json.optString("token"), phone);
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
        */
    }

    private void serverRegister() {
        String name = registerName.getText().toString().trim();
        final String phone = normalizePhone(registerPhone.getText().toString().trim());
        String password = registerPassword.getText().toString();
        String confirm = registerConfirmPassword.getText().toString();
        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isSafaricomNumber(phone)) {
            Toast.makeText(this, "Only Safaricom numbers are supported", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            org.json.JSONObject body = new org.json.JSONObject();
            body.put("phone", phone);
            body.put("name", name);
            body.put("password", password);
            Api.post("/register", body, null, (ok, json, error) -> {
                if (!ok) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, "Account created! You received 1,000 Coins. Please log in.", Toast.LENGTH_LONG).show();
                registerName.setText("");
                registerPhone.setText("");
                registerPassword.setText("");
                registerConfirmPassword.setText("");
                registerSection.setVisibility(View.GONE);
                loginPhone.setText(phone);
            });
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin() {
        String phone = normalizePhone(loginPhone.getText().toString().trim());
        String password = loginPassword.getText().toString();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter phone number and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isSafaricomNumber(phone)) {
            Toast.makeText(this, "Enter a valid Safaricom number", Toast.LENGTH_SHORT).show();
            return;
        }

        String storedPassword = prefs.getString(phone + "_password", null);
        if (storedPassword == null) {
            Toast.makeText(this, "No account found for this number. Please register.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!storedPassword.equals(password)) {
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(AuthActivity.this, HomeActivity.class));
        finish();
    }

    private void handleRegister() {
        String name = registerName.getText().toString().trim();
        String phone = normalizePhone(registerPhone.getText().toString().trim());
        String password = registerPassword.getText().toString();
        String confirmPassword = registerConfirmPassword.getText().toString();

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isSafaricomNumber(phone)) {
            Toast.makeText(this, "Only Safaricom numbers are supported", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (prefs.contains(phone + "_password")) {
            Toast.makeText(this, "An account with this number already exists", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(phone + "_name", name);
        editor.putString(phone + "_password", password);
        editor.apply();

        Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_LONG).show();

        registerName.setText("");
        registerPhone.setText("");
        registerPassword.setText("");
        registerConfirmPassword.setText("");
        registerSection.setVisibility(View.GONE);
        loginPhone.setText(phone);
    }
}
