package com.maliks.applocker.xtreme.ui.intruders

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class IntrudersPhotosViewModel @Inject constructor(
    val app: Application
) : RxAwareViewModel() {

    private val intruderListViewState = MutableLiveData<IntrudersViewState>()

    init {
        loadIntruderPhotos()
    }

    fun getIntruderListViewState(): LiveData<IntrudersViewState> =
        intruderListViewState

    private fun loadIntruderPhotos() {
        val photosObservable = Single.create<List<Uri>> { emitter ->
            val photoUris = mutableListOf<Uri>()

            // Define the collection based on the Android version
            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            // Define the columns to retrieve
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME
            )

            // Define the selection criteria
            val selection: String
            val selectionArgs: Array<String>

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, use RELATIVE_PATH
                selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
                selectionArgs = arrayOf("${Environment.DIRECTORY_PICTURES}/Locked/Intruders/")
            } else {
                // For Android 9 and below, use DATA (deprecated in API 29)
                selection = "${MediaStore.Images.Media.DATA} LIKE ?"
                selectionArgs = arrayOf("%${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path}/Locked/Intruders/%")
            }

            // Define the sort order
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            // Query the MediaStore
            app.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        collection,
                        id
                    )
                    photoUris.add(contentUri)
                }
            } ?: run {
                emitter.onError(Exception("Failed to query MediaStore"))
                return@create
            }

            emitter.onSuccess(photoUris)
        }

        disposables += photosObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { uris ->
                    intruderListViewState.value = IntrudersViewState(mapToViewState(uris))
                },
                { error -> Log.e("IntrudersViewModel", "Error loading photos: ${error.message}") })
    }


    fun deleteAllPhotos() {
        val currentPhotos = intruderListViewState.value?.intruderPhotoItemViewStateList ?: return

        val deletePhotosObservable = Single.create<Boolean> { emitter ->
            try {
                for (photo in currentPhotos) {
                    app.contentResolver.delete(photo.getPhotoUri(), null, null)
                }
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        disposables += deletePhotosObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    intruderListViewState.value = IntrudersViewState(emptyList())
                },
                { error -> Log.e("IntrudersViewModel", "Error deleting photos: ${error.message}") }
            )
    }

    private fun mapToViewState(uris: List<Uri>): List<IntruderPhotoItemViewState> {
        return uris.map { IntruderPhotoItemViewState(it) }
    }
}
