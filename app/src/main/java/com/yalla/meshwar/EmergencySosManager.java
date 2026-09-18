package com.yalla.meshwar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class EmergencySosManager {
    // رقم شرطة النجدة المصرية الموحد
    private static final String EGYPT_POLICE_NUMBER = "122";

    public static void callEgyptPoliceAndSendLocation(Context context, String userId, double latitude, double longitude) {
        // 1. تسجيل استغاثة الطوارئ في Firebase
        try {
            DatabaseReference sosRef = FirebaseDatabase.getInstance().getReference("egypt_police_sos");
            String emergencyId = sosRef.push().getKey();
            HashMap<String, Object> emergencyData = new HashMap<>();
            emergencyData.put("userId", userId);
            emergencyData.put("latitude", latitude);
            emergencyData.put("longitude", longitude);
            emergencyData.put("locationUrl", "https://maps.google.com/?q=" + latitude + "," + longitude);
            emergencyData.put("timestamp", System.currentTimeMillis());
            emergencyData.put("status", "ACTIVE_EMERGENCY");

            if (emergencyId != null) {
                sosRef.child(emergencyId).setValue(emergencyData);
            }
        } catch (Exception e) {
            Log.e("EmergencySosManager", "Firebase update exception: " + e.getMessage());
        }

        Toast.makeText(context, "جاري الاتصال بشرطة النجدة المصرية (122)...", Toast.LENGTH_LONG).show();

        // 2. الاتصال الفوري برقم 122
        Intent policeCallIntent = new Intent(Intent.ACTION_CALL);
        policeCallIntent.setData(Uri.parse("tel:" + EGYPT_POLICE_NUMBER));
        policeCallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(policeCallIntent);
        } catch (SecurityException e) {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + EGYPT_POLICE_NUMBER));
            dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(dialIntent);
        }
    }
}
