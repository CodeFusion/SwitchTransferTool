package ca.codefusion.switchtransfertool.interactors

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import ca.codefusion.switchtransfertool.BuildConfig
import ca.codefusion.switchtransfertool.R

import ca.codefusion.switchtransfertool.data.models.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class GalleryInteractor {

    private val DELETE_PERMISSION_REQUEST = 1000

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getImages(context: Context): List<MediaFile> {
        var imageList = mutableListOf<MediaFile>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%${NetworkInteractor.getAppPhotoPath()}%")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        context.applicationContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            imageList = addImagesFromCursor(cursor)
        }

        return imageList
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun addImagesFromCursor(cursor: Cursor): MutableList<MediaFile> {
        val images = mutableListOf<MediaFile>()

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
        val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

        while (cursor.moveToNext()) {
            try {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)

                // 20190109223101 2019-01-09-22:31:01
                val dateString = displayName.substring(0, 14)
                val dateTaken =
                    LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                val dateAdded =
                    LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(
                            cursor.getString(dateAddedColumn).toLong()
                        ), TimeZone.getDefault().toZoneId()
                    )
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val image = MediaFile(
                    id = id,
                    type = MediaFile.MediaType.IMAGE,
                    displayName = displayName,
                    dateTaken = dateTaken,
                    dateAdded = dateAdded,
                    contentUri = contentUri
                )

                images += image
            } catch (e: DateTimeParseException) {
                continue
            }
        }
        return images
    }

    @TargetApi(Build.VERSION_CODES.Q)
    fun getVideos(context: Context): List<MediaFile> {
        var videoList = mutableListOf<MediaFile>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DATE_ADDED
        )
        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%${NetworkInteractor.getAppVideoPath()}%")
        val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"
        context.applicationContext.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            videoList = addVideosFromCursor(cursor)
        }

        return videoList
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun addVideosFromCursor(cursor: Cursor): MutableList<MediaFile> {
        val videos = mutableListOf<MediaFile>()

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

        while (cursor.moveToNext()) {
            try {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                // 20190109223101 2019-01-09-22:31:01
                val dateString = displayName.substring(0, 14)
                val dateTaken =
                    LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                val dateAdded =
                    LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(
                            cursor.getString(dateAddedColumn).toLong()
                        ), TimeZone.getDefault().toZoneId()
                    )
                val duration = cursor.getString(durationColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val video = MediaFile(
                    id = id,
                    type = MediaFile.MediaType.VIDEO,
                    durationString = duration,
                    displayName = displayName,
                    dateTaken = dateTaken,
                    dateAdded = dateAdded,
                    contentUri = contentUri
                )
                videos += video
            } catch (e: DateTimeParseException) {
                continue
            }
        }
        return videos
    }

    suspend fun deleteFile(mediaFile: MediaFile, context: Context, activity: Activity) = withContext(Dispatchers.IO) {
        try {
            when (mediaFile.type) {
                MediaFile.MediaType.IMAGE -> {
                    context.applicationContext.contentResolver.delete(
                        mediaFile.contentUri,
                        "${MediaStore.Images.Media._ID} = ?",
                        arrayOf(mediaFile.id.toString())
                    )
                }
                MediaFile.MediaType.VIDEO -> {
                    context.applicationContext.contentResolver.delete(
                        mediaFile.contentUri,
                        "${MediaStore.Video.Media._ID} = ?",
                        arrayOf(mediaFile.id.toString())
                    )
                }
            }
        } catch (securityException: SecurityException) {
            val recoverableSecurityException =
                securityException as? RecoverableSecurityException

            recoverableSecurityException?.let {
                startIntentSenderForResult(
                    activity,
                    recoverableSecurityException.userAction.actionIntent.intentSender,
                    DELETE_PERMISSION_REQUEST,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }
        }
    }
}