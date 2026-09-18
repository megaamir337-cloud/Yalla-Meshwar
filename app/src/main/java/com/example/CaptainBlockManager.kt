package com.example

import com.yalla.meshwar.CaptainBlockManager as YallaCaptainBlockManager

object CaptainBlockManager {
    const val MAX_REJECTIONS: Int = YallaCaptainBlockManager.MAX_REJECTIONS
    const val MAX_REPORTS: Int = YallaCaptainBlockManager.MAX_REPORTS
    const val BLOCK_DURATION_MS: Long = YallaCaptainBlockManager.BLOCK_DURATION_MS

    fun interface BlockCheckCallback {
        fun onResult(isBlocked: Boolean, remainingTimeMs: Long)
    }

    @JvmStatic
    fun recordTripRejection(captainId: String) {
        YallaCaptainBlockManager.recordTripRejection(captainId)
    }

    @JvmStatic
    fun resetRejectionsOnTripAccept(captainId: String) {
        YallaCaptainBlockManager.resetRejectionsOnTripAccept(captainId)
    }

    @JvmStatic
    fun recordCaptainReport(captainId: String, reason: String) {
        YallaCaptainBlockManager.recordCaptainReport(captainId, reason)
    }

    @JvmStatic
    fun checkIfCaptainIsBlocked(
        captainId: String,
        callback: YallaCaptainBlockManager.BlockCheckCallback
    ) {
        YallaCaptainBlockManager.checkIfCaptainIsBlocked(captainId, callback)
    }

    @JvmStatic
    fun getLocalConsecutiveRejections(captainId: String): Long {
        return YallaCaptainBlockManager.getLocalConsecutiveRejections(captainId)
    }

    @JvmStatic
    fun getLocalTotalReports(captainId: String): Long {
        return YallaCaptainBlockManager.getLocalTotalReports(captainId)
    }

    @JvmStatic
    fun clearLocalState() {
        YallaCaptainBlockManager.clearLocalState()
    }
}
