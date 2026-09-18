package com.example

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MeshwarRepository
import com.example.model.RatingRecord
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MalakiGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.YallaMeshwarTheme

object RatingDialog {
    @JvmStatic
    fun showRatingDialog(context: Context, targetUserId: String, tripId: String) {
        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val composeView = ComposeView(context).apply {
            setContent {
                YallaMeshwarTheme {
                    RatingDialogContent(
                        targetUserId = targetUserId,
                        tripId = tripId,
                        onDismiss = { dialog.dismiss() },
                        onRatingSubmitted = { rating ->
                            Toast.makeText(context, "شكراً لتقييمك، رأيك يهمنا دائماً!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                    )
                }
            }
        }

        dialog.setContentView(composeView)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    @Composable
    fun RatingDialogModal(
        targetUserId: String,
        tripId: String,
        onDismiss: () -> Unit,
        onSubmitted: (Float) -> Unit
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            RatingDialogContent(
                targetUserId = targetUserId,
                tripId = tripId,
                onDismiss = onDismiss,
                onRatingSubmitted = onSubmitted
            )
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun RatingDialogContent(
        targetUserId: String,
        tripId: String,
        onDismiss: () -> Unit,
        onRatingSubmitted: (Float) -> Unit
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current
        var rating by remember { mutableFloatStateOf(5.0f) }
        var comment by remember { mutableStateOf("") }
        var selectedTags by remember { mutableStateOf(setOf("سائق محترم", "قيادة آمنة")) }

        val tags = listOf(
            "سائق محترم",
            "التزام بالوقت",
            "سيارة نظيفة",
            "قيادة آمنة",
            "تكييف ممتاز",
            "طريق سريع"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dialog_rating"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .border(1.5.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تقييم الكابتن والرحلة",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ساعدنا في الحفاظ على أفضل جودة وأمان للمجتمع",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Interactive RatingBar (testTag: ratingBar)
                Row(
                    modifier = Modifier
                        .testTag("ratingBar")
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { starIndex ->
                        val isSelected = starIndex <= rating
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) GoldPrimary.copy(alpha = 0.18f) else DarkSurfaceElevated
                                )
                                .clickable { rating = starIndex.toFloat() }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "نجمة $starIndex",
                                tint = if (isSelected) GoldPrimary else TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (rating.toInt()) {
                        5 -> "ممتاز جداً ⭐⭐⭐⭐⭐"
                        4 -> "جيد جداً ⭐⭐⭐⭐"
                        3 -> "متوسط ⭐⭐⭐"
                        2 -> "يحتاج تحسين ⭐⭐"
                        else -> "غير راضٍ ⭐"
                    },
                    color = if (rating >= 4) MalakiGreen else GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Quick feedback tags
                Text(
                    text = "ما الذي أعجبك في المشوار؟",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldPrimary else DarkBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tag,
                                color = if (isSelected) GoldPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Comment text field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("أضف تعليقاً إضافياً (اختياري)...", color = TextSecondary, fontSize = 12.sp) },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // btnSubmitRating button
                Button(
                    onClick = {
                        val record = RatingRecord(
                            id = "rating_${System.currentTimeMillis()}",
                            targetUserId = targetUserId,
                            tripId = tripId,
                            rating = rating,
                            comment = if (comment.isNotBlank()) comment else selectedTags.joinToString(", ")
                        )
                        MeshwarRepository.submitRating(record)
                        onRatingSubmitted(rating)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btnSubmitRating"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "إرسال التقييم",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option to report captain (recordCaptainReport)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val reportReason = if (comment.isNotBlank()) comment else "بلاغ سلوك أو مخالفة تسعيرة"
                            CaptainBlockManager.recordCaptainReport(targetUserId, reportReason)
                            Toast.makeText(
                                context,
                                "تم تسجيل البلاغ ضد الكابتن ($reportReason). شكراً لمساعدتنا.",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismiss()
                        }
                        .padding(vertical = 6.dp)
                        .testTag("btnReportCaptain"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تقديم بلاغ أو شكوى ضد الكابتن",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
