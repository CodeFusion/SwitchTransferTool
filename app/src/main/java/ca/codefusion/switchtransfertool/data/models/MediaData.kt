package ca.codefusion.switchtransfertool.data.models

import com.google.gson.annotations.SerializedName

data class MediaData(
    @SerializedName("FileType")
    val fileType: FileType,
    @SerializedName("ConsoleName")
    val consoleName: String,
    @SerializedName("FileNames")
    val fileNames: List<String>
) {

    enum class FileType {
        @SerializedName("photo")
        PHOTO,

        @SerializedName("movie")
        VIDEO
    }
}