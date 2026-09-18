package com.yalla.meshwar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserTripActivity extends AppCompatActivity {

    private LinearLayout layoutCaptainDetails;
    private ImageView imgCaptainPhoto;
    private TextView tvCaptainName, tvVehicleAndPlate, tvCaptainRating;
    private ImageButton btnCallCaptain;

    private String currentTripId = "TRIP_12345";
    private String captainPhoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_trip_status);

        if (getIntent().hasExtra("TRIP_ID")) {
            String tripId = getIntent().getStringExtra("TRIP_ID");
            if (tripId != null && !tripId.trim().isEmpty()) {
                currentTripId = tripId;
            }
        }

        layoutCaptainDetails = findViewById(R.id.layoutCaptainDetails);
        imgCaptainPhoto = findViewById(R.id.imgCaptainPhoto);
        tvCaptainName = findViewById(R.id.tvCaptainName);
        tvVehicleAndPlate = findViewById(R.id.tvVehicleAndPlate);
        tvCaptainRating = findViewById(R.id.tvCaptainRating);
        btnCallCaptain = findViewById(R.id.btnCallCaptain);

        btnCallCaptain.setOnClickListener(v -> {
            if (captainPhoneNumber != null && !captainPhoneNumber.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + captainPhoneNumber));
                startActivity(intent);
            }
        });

        listenForTripAcceptance();
    }

    private void listenForTripAcceptance() {
        try {
            DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("trips").child(currentTripId);
            tripRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String status = snapshot.child("status").getValue(String.class);
                        if ("ACCEPTED".equals(status)) {
                            // تم قبول الرحلة من الكابتن، استخراج البيانات
                            String name = snapshot.child("captainName").getValue(String.class);
                            String vehicle = snapshot.child("captainVehicle").getValue(String.class);
                            String plate = snapshot.child("captainPlate").getValue(String.class);
                            String photoUrl = snapshot.child("captainPhotoUrl").getValue(String.class);
                            Double rating = snapshot.child("captainRating").getValue(Double.class);
                            captainPhoneNumber = snapshot.child("captainPhone").getValue(String.class);

                            // تحديث الواجهة
                            if (tvCaptainName != null) tvCaptainName.setText(name != null ? name : "كابتن يلا مشوار");
                            if (tvVehicleAndPlate != null) tvVehicleAndPlate.setText((vehicle != null ? vehicle : "سكوتر") + " | " + (plate != null ? plate : "أ ب ج 1234"));
                            if (tvCaptainRating != null) tvCaptainRating.setText("⭐ " + (rating != null ? rating : "4.9"));

                            // تحميل الصورة بواسطة Glide
                            if (photoUrl != null && !photoUrl.isEmpty() && imgCaptainPhoto != null) {
                                try {
                                    Glide.with(UserTripActivity.this)
                                            .load(photoUrl)
                                            .circleCrop()
                                            .into(imgCaptainPhoto);
                                } catch (Exception e) {
                                    imgCaptainPhoto.setImageResource(R.drawable.ic_default_avatar);
                                }
                            }

                            // إظهار بطاقة تفاصيل الكابتن
                            if (layoutCaptainDetails != null) {
                                layoutCaptainDetails.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } catch (Exception e) {
            // Fallback
        }
    }
}
