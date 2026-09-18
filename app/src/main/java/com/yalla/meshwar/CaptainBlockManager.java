package com.yalla.meshwar;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class CaptainBlockManager {
    private static final String TAG = "CaptainBlockManager";
    public static final int MAX_REJECTIONS = 3;
    public static final int MAX_REPORTS = 3;
    public static final long BLOCK_DURATION_MS = 24 * 60 * 60 * 1000L; // حظر 24 ساعة

    // تخزين محلي لضمان العمل حتى بدون اتصال Firebase
    private static final ConcurrentHashMap<String, Long> localRejections = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> localReports = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> localBlockedUntil = new ConcurrentHashMap<>();

    public interface BlockCheckCallback {
        void onBlocked(long remainingTimeMs);
        void onAllowed();
    }

    // 1. تسجيل رفض الرحلة
    public static void recordTripRejection(String captainId) {
        if (captainId == null || captainId.trim().isEmpty()) return;

        // تحديث محلي
        long currentLocal = localRejections.getOrDefault(captainId, 0L) + 1;
        localRejections.put(captainId, currentLocal);
        if (currentLocal >= MAX_REJECTIONS) {
            long blockUntil = System.currentTimeMillis() + BLOCK_DURATION_MS;
            localBlockedUntil.put(captainId, blockUntil);
            localRejections.put(captainId, 0L);
        }

        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("captains").child(captainId);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long consecutiveRejections = 0;
                    if (snapshot.child("consecutiveRejections").exists()) {
                        Long val = snapshot.child("consecutiveRejections").getValue(Long.class);
                        if (val != null) consecutiveRejections = val;
                    }
                    consecutiveRejections++;
                    HashMap<String, Object> updates = new HashMap<>();
                    updates.put("consecutiveRejections", consecutiveRejections);

                    if (consecutiveRejections >= MAX_REJECTIONS) {
                        long blockUntil = System.currentTimeMillis() + BLOCK_DURATION_MS;
                        updates.put("blockedUntil", blockUntil);
                        updates.put("consecutiveRejections", 0); // تصفير بعد الحظر
                        localBlockedUntil.put(captainId, blockUntil);
                    }
                    ref.updateChildren(updates);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "recordTripRejection onCancelled: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Firebase unavailable for recordTripRejection: " + e.getMessage());
        }
    }

    // 2. تصفير الرفض عند قبول رحلة بنجاح
    public static void resetRejectionsOnTripAccept(String captainId) {
        if (captainId == null || captainId.trim().isEmpty()) return;
        localRejections.put(captainId, 0L);
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("captains").child(captainId);
            ref.child("consecutiveRejections").setValue(0);
        } catch (Exception e) {
            Log.e(TAG, "Firebase unavailable for resetRejectionsOnTripAccept: " + e.getMessage());
        }
    }

    // 3. تسجيل بلاغ ضد الكابتن
    public static void recordCaptainReport(String captainId, String reason) {
        if (captainId == null || captainId.trim().isEmpty()) return;

        // تحديث محلي
        long currentReports = localReports.getOrDefault(captainId, 0L) + 1;
        localReports.put(captainId, currentReports);
        if (currentReports >= MAX_REPORTS) {
            long blockUntil = System.currentTimeMillis() + BLOCK_DURATION_MS;
            localBlockedUntil.put(captainId, blockUntil);
            localReports.put(captainId, 0L);
        }

        try {
            DatabaseReference captainRef = FirebaseDatabase.getInstance().getReference("captains").child(captainId);
            captainRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long totalReports = 0;
                    if (snapshot.child("totalReports").exists()) {
                        Long val = snapshot.child("totalReports").getValue(Long.class);
                        if (val != null) totalReports = val;
                    }
                    totalReports++;
                    HashMap<String, Object> updates = new HashMap<>();
                    updates.put("totalReports", totalReports);
                    updates.put("lastReportReason", reason != null ? reason : "بلاغ من راكب");
                    updates.put("lastReportTimestamp", System.currentTimeMillis());

                    if (totalReports >= MAX_REPORTS) {
                        long blockUntil = System.currentTimeMillis() + BLOCK_DURATION_MS;
                        updates.put("blockedUntil", blockUntil);
                        updates.put("totalReports", 0); // تصفير بعد فرض الحظر
                        localBlockedUntil.put(captainId, blockUntil);
                    }
                    captainRef.updateChildren(updates);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "recordCaptainReport onCancelled: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Firebase unavailable for recordCaptainReport: " + e.getMessage());
        }
    }

    // 4. فحص حالة حظر الكابتن
    public static void checkIfCaptainIsBlocked(String captainId, BlockCheckCallback callback) {
        if (captainId == null || captainId.trim().isEmpty()) {
            callback.onAllowed();
            return;
        }

        // فحص الحالة المحلية أولاً
        Long localUntil = localBlockedUntil.get(captainId);
        long currentTime = System.currentTimeMillis();
        if (localUntil != null) {
            if (currentTime < localUntil) {
                callback.onBlocked(localUntil - currentTime);
                return;
            } else {
                localBlockedUntil.remove(captainId);
            }
        }

        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("captains").child(captainId).child("blockedUntil");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Long blockedUntilVal = snapshot.getValue(Long.class);
                        long blockedUntil = blockedUntilVal != null ? blockedUntilVal : 0L;
                        long now = System.currentTimeMillis();
                        if (now < blockedUntil) {
                            // الكابتن محظور
                            long remainingTime = blockedUntil - now;
                            localBlockedUntil.put(captainId, blockedUntil);
                            callback.onBlocked(remainingTime);
                        } else {
                            // انتهت فترة الحظر (24 ساعة)
                            ref.removeValue(); // إزالة الحظر
                            localBlockedUntil.remove(captainId);
                            callback.onAllowed();
                        }
                    } else {
                        callback.onAllowed();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onAllowed();
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Firebase unavailable for checkIfCaptainIsBlocked, fallback to local: " + e.getMessage());
            callback.onAllowed();
        }
    }

    // مساعدات الفحص المحلي
    public static long getLocalConsecutiveRejections(String captainId) {
        return localRejections.getOrDefault(captainId, 0L);
    }

    public static long getLocalTotalReports(String captainId) {
        return localReports.getOrDefault(captainId, 0L);
    }

    public static void clearLocalState() {
        localRejections.clear();
        localReports.clear();
        localBlockedUntil.clear();
    }
}
