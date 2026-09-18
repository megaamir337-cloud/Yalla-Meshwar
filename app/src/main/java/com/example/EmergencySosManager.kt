package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

object EmergencySosManager {
    // رقم شرطة النجدة المصرية الموحد
    const val EGYPT_POLICE_NUMBER = "122"

    @JvmStatic
    fun callEgyptPoliceAndSendLocation(
        context: Context,
        userId: String,
        latitude: Double,
        longitude: Double
    ) {
        // 1. تسجيل استغاثة الطوارئ في Firebase
        try {
            val sosRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("egypt_police_sos")
            val emergencyId = sosRef.push().key
            val emergencyData = HashMap<String, Any>()
            emergencyData["userId"] = userId
            emergencyData["latitude"] = latitude
            emergencyData["longitude"] = longitude
            emergencyData["locationUrl"] = "https://maps.google.com/?q=$latitude,$longitude"
            emergencyData["timestamp"] = System.currentTimeMillis()
            emergencyData["status"] = "ACTIVE_EMERGENCY"
            if (emergencyId != null) {
                sosRef.child(emergencyId).setValue(emergencyData)
            }
        } catch (e: Exception) {
            Log.e("EmergencySosManager", "Error storing emergency to Firebase: ${e.message}")
        }

        Toast.makeText(
            context,
            "جاري الاتصال بشرطة النجدة (122)...",
            Toast.LENGTH_LONG
        ).show()

        // 2. الاتصال الفوري برقم 122
        val policeCallIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$EGYPT_POLICE_NUMBER")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(policeCallIntent)
        } catch (e: SecurityException) {
            // فتح لوحة الاتصال إذا تعذر الاتصال المباشر
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$EGYPT_POLICE_NUMBER")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)
        }
    }
}
