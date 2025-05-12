package com.biztools.stockcount.ui.utilities

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.TimeUnit

@ExperimentalGetImage
class BarcodeAnalyzer(
    private val onBarcodeDetected: (codes: List<Barcode>, width: Int) -> Unit,
    private val onFailed: (ex: Exception) -> Unit = {},
    private val onStartAnalyze: (img: InputImage) -> Unit
) :
    ImageAnalysis.Analyzer {
    private var lastAnalysisTimestamp = 1L

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalysisTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
            image.image?.let {
                val scanner = BarcodeScanning.getClient()
                val processNeededImage =
                    InputImage.fromMediaImage(it, image.imageInfo.rotationDegrees)
                onStartAnalyze(processNeededImage)
                scanner.process(processNeededImage).addOnSuccessListener { barcodes ->
                    if (barcodes.isEmpty()) return@addOnSuccessListener
                    onBarcodeDetected(barcodes, image.width)
                }.addOnFailureListener(onFailed).addOnCompleteListener { image.close() }
            }
            lastAnalysisTimestamp = currentTimestamp
        } else image.close()
    }
}