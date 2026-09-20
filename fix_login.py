import re

path = "app/src/main/java/com/tukokadi/app/AuthActivity.java"
with open(path, "r") as f:
    content = f.read()

start_marker = "private void serverLogin() {"
end_marker = "private void serverRegister() {"

start = content.find(start_marker)
end = content.find(end_marker)

if start == -1 or end == -1:
    print("ERROR: markers not found, aborting.")
    exit(1)

new_method = '''private void serverLogin() {
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

    '''

new_content = content[:start] + new_method + content[end:]

with open(path, "w") as f:
    f.write(new_content)

print("Done. serverLogin() replaced successfully.")
