package com.wanderers.kidkit;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    private static final String REGISTER_REQUEST_URL = "http://cardioapp.000webhostapp.com/register.php";
    private Map<String, String> params;

    public RegisterRequest(String firstname, String lastname, String gender, String email, String password, String c_password, Response.Listener<String> listener) {
        super(Method.POST, REGISTER_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("firstname", firstname);
        params.put("lastname", lastname);
        params.put("gender", gender);
        params.put("email", email);
        params.put("password", password);
        params.put("c_password", c_password);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
