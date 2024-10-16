package com.maliks.applocker.xtreme.ui.browser.bookmarks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.maliks.applocker.xtreme.data.database.bookmark.BookmarkDao
import com.maliks.applocker.xtreme.data.database.bookmark.BookmarkEntity
import com.maliks.applocker.xtreme.ui.RxAwareViewModel
import com.maliks.applocker.xtreme.util.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class BookmarksViewModel @Inject constructor(bookmarkDao: BookmarkDao) : RxAwareViewModel() {

    private val bookmarkListLiveData = MutableLiveData<List<BookmarkEntity>>()

    init {
        disposables += bookmarkDao.getBookmarks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                bookmarkListLiveData.value = it
            }
    }

    fun getBookmarksLiveData(): LiveData<List<BookmarkEntity>> = bookmarkListLiveData

}