package com.example.exampleproject.videoeditor.impl

import android.graphics.Bitmap
import com.banuba.sdk.ve.effects.WatermarkProvider

class IntegrationAppWatermarkProvider : WatermarkProvider {

    /**
     * Provide your own watermark image
     * */
    override fun getWatermarkBitmap(): Bitmap? {
        return null
    }
}