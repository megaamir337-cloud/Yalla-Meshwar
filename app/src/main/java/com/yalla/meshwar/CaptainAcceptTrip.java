package com.yalla.meshwar;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class CaptainAcceptTrip {
    private static final String TAG = "CaptainAcceptTrip";

    public static void acceptTripAndSendDetails(String tripId, CaptainDetails captain) {
        if (tripId == null || captain == null) return;

        try {
            DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("trips").child(tripId);
            HashMap<String, Object> updates = new HashMap<>();
            updates.put("status", "ACCEPTED"); // تم القبول
            updates.put("captainId", captain.captainId);
            updates.put("captainName", captain.name);
            updates.put("captainPhone", captain.phone);
            updates.put("captainVehicle", captain.vehicleType);
            updates.put("captainPlate", captain.plateNumber);
            updates.put("captainPhotoUrl", captain.photoUrl);
            updates.put("captainRating", captain.rating);

            // تحديث في Firebase
            tripRef.updateChildren(updates);
        } catch (Exception e) {
            Log.e(TAG, "Firebase unavailable for acceptTripAndSendDetails: " + e.getMessage());
        }

        // تصفير عداد الرفض المتتالي للكابتن
        CaptainBlockManager.resetRejectionsOnTripAccept(captain.captainId);

        // تحديث المستودع المحلي إن وجد
        try {
            com.example.model.MeshwarRepository.INSTANCE.onCaptainAcceptedTrip(
                tripId,
                captain.captainId,
                captain.name,
                captain.phone,
                captain.vehicleType,
                captain.plateNumber,
                captain.photoUrl,
                captain.rating
            );
        } catch (Throwable ignored) {
        }
    }
}
