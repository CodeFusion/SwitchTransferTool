package ca.codefusion.switchtransfertool.data.models

import android.net.Uri
import java.time.LocalDateTime
import kotlin.math.roundToInt

data class MediaFile(
    val id: Long,
    val type: MediaType,
    val durationString: String? = null,
    val displayName: String,
    val dateTaken: LocalDateTime,
    val dateAdded: LocalDateTime,
    val contentUri: Uri
) {
    val duration: Int = ((durationString?.toDouble() ?: 0.0) / 1000).roundToInt()

    enum class MediaType { IMAGE, VIDEO }
}
