package com.maliks.applocker.xtreme.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.util.AdManager
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity<VM : ViewModel> : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected lateinit var viewModel: VM

    abstract fun getViewModel(): Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdManager.getInstance(this, this.getString(R.string.dashboard_ad_inter));
        window.setBackgroundDrawable(ColorDrawable(0x001c1c));

        viewModel = ViewModelProvider(this, viewModelFactory).get(getViewModel())
    }
    
}
