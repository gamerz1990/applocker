package com.maliks.applocker.xtreme.ui.browser

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.maliks.applocker.xtreme.databinding.ActivityBrowserBinding
import com.maliks.applocker.xtreme.ui.BaseActivity
import android.webkit.*
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.data.database.bookmark.BookmarkEntity
import com.maliks.applocker.xtreme.ui.browser.analytics.BrowserAnalytics
import com.maliks.applocker.xtreme.ui.browser.bookmarks.BookmarksDialog
import com.maliks.applocker.xtreme.ui.browser.resolver.UrlResolver.FACEBOOK
import com.maliks.applocker.xtreme.ui.browser.resolver.UrlResolver.INSTAGRAM
import com.maliks.applocker.xtreme.ui.browser.resolver.UrlResolver.TWITTER
import com.maliks.applocker.xtreme.ui.browser.resolver.UrlResolver.YOUTUBE
import com.maliks.applocker.xtreme.util.extensions.hideKeyboard


class BrowserActivity : BaseActivity<BrowserViewModel>(), BookmarksDialog.BookmarkItemSelectListener {

    private lateinit var binding: ActivityBrowserBinding

    private val browserClient = object : WebViewClient() {

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            viewModel.complete()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            viewModel.load(url!!)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            removeFocusFromBar()
            hideKeyboard(binding.edittextUrl)
        }
    }

    private val chromeClient = object : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            viewModel.updateLoadingProgress(newProgress)
        }
    }

    override fun getViewModel(): Class<BrowserViewModel> = BrowserViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_browser)

        initializeWebView()

        binding.imageViewBack.setOnClickListener { finish() }

        binding.layoutFacebook.setOnClickListener {
            viewModel.load(FACEBOOK)
            BrowserAnalytics.sendQuickBrowsingClicked(this, FACEBOOK)
        }

        binding.layoutInstagram.setOnClickListener {
            viewModel.load(INSTAGRAM)
            BrowserAnalytics.sendQuickBrowsingClicked(this, INSTAGRAM)
        }

        binding.layoutTwitter.setOnClickListener {
            viewModel.load(TWITTER)
            BrowserAnalytics.sendQuickBrowsingClicked(this, TWITTER)
        }

        binding.layoutYoutube.setOnClickListener {
            viewModel.load(YOUTUBE)
            BrowserAnalytics.sendQuickBrowsingClicked(this, YOUTUBE)
        }

        binding.imageViewBookmarks.setOnClickListener {
            BookmarksDialog
                .newInstance()
                .show(supportFragmentManager, "")
        }

        binding.imageViewAddBookmark.setOnClickListener {
            if (viewModel.isCurrentBookmarked()) {
                viewModel.removeFromBookmark()
            } else {
                viewModel.addToBookmarks()
            }
        }

        binding.edittextUrl.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.load(binding.edittextUrl.text.toString())
                true
            }
            false
        }

        viewModel.getBrowserViewStateLiveData().observe(this, Observer {
            binding.edittextUrl.clearFocus()
            binding.viewState = it
            binding.executePendingBindings()

            if (it.webPageState == WebPageState.REQUEST) binding.webView.loadUrl(it.url)
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    when {
                        binding.webView.canGoBack() -> goBackInWebView()
                        viewModel.isResetMode() -> finish()
                        else -> viewModel.reset()
                    }
                    return true
                }
            }

        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBookmarkItemSelected(bookmarkEntity: BookmarkEntity) {
        viewModel.load(bookmarkEntity.url)
    }

    private fun initializeWebView() {
        // Disable cookies
        CookieManager.getInstance().setAcceptCookie(false)
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        with(binding.webView) {
            webChromeClient = chromeClient
            webViewClient = browserClient
            settings.apply {
                javaScriptEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                // Deprecated methods removed
                // setAppCacheEnabled(false)
                // savePassword = false
                saveFormData = false
                // Additional settings (optional)
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            }
            clearHistory()
            clearCache(true)
            clearFormData()
        }
    }


    private fun removeFocusFromBar() {
        with(binding.edittextUrl) {
            isFocusableInTouchMode = false
            isFocusable = false
            isFocusableInTouchMode = true
            isFocusable = true
        }
    }

    private fun goBackInWebView() {
        val mWebBackForwardList = binding.webView.copyBackForwardList()
        val historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.currentIndex - 1).url
        viewModel.completeWithUrl(historyUrl)
        binding.webView.goBack()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BrowserActivity::class.java)
        }
    }
}