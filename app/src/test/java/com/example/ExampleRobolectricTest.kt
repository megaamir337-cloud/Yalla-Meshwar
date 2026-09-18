package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("يلا مشوار", appName)
  }

  @Test
  fun `test trip price calculation for categories`() {
    val toktokPrice = TripPriceCalculator.calculateSuggestedPrice("توك توك", 5.0)
    val scooterPrice = TripPriceCalculator.calculateSuggestedPrice("سكوتر", 5.0)
    val malakiPrice = TripPriceCalculator.calculateSuggestedPrice("ملاكي", 5.0)

    // TokTok: 10.0 + (5.0 * 5.0) = 35.0
    assertEquals(35.0, toktokPrice, 0.01)
    // Scooter: 12.0 + (5.0 * 6.0) = 42.0
    assertEquals(42.0, scooterPrice, 0.01)
    // Malaki: 20.0 + (5.0 * 10.0) = 70.0
    assertEquals(70.0, malakiPrice, 0.01)
  }

  @Test
  fun `test captain block manager limits`() {
    val capId = "TEST_CAPTAIN_1"
    val rejectionsBefore = CaptainBlockManager.getLocalConsecutiveRejections(capId)
    assertEquals(0L, rejectionsBefore)

    CaptainBlockManager.recordTripRejection(capId)
    val rejectionsAfter = CaptainBlockManager.getLocalConsecutiveRejections(capId)
    assertEquals(1L, rejectionsAfter)
  }
}
