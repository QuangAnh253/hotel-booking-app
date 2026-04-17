package com.nhom4.hotelbooking.utils;

import android.util.Log;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EmailUtils {
    public static void sendOtpEmail(String toEmail, String otpCode, String templateId) {
        new Thread(() -> {
            try {
                String serviceId  = "service_hotel_booking";
                String publicKey  = "qGw9f1tPmRI72Ns1K";

                // Sử dụng user_email để khớp với template HTML của bạn
                String jsonBody = "{"
                        + "\"service_id\":\""  + serviceId  + "\","
                        + "\"template_id\":\"" + templateId + "\","
                        + "\"user_id\":\""     + publicKey  + "\","
                        + "\"template_params\":{"
                        + "\"user_email\":\"" + toEmail  + "\","
                        + "\"otp_code\":\""    + otpCode  + "\""
                        + "}}";

                URL url = new URL("https://api.emailjs.com/api/v1.0/email/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes("UTF-8"));
                os.flush();
                os.close();

                Log.d("OTP_EMAIL", "Sent to: " + toEmail + " | Response: " + conn.getResponseCode());
                conn.disconnect();
            } catch (Exception e) {
                Log.e("OTP_EMAIL", "Error: " + e.getMessage());
            }
        }).start();
    }
}