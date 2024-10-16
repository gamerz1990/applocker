package com.maliks.applocker.xtreme.ui.intruders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.ActivityIntrudersPhotosBinding
import com.maliks.applocker.xtreme.ui.BaseActivity
import com.maliks.applocker.xtreme.util.delegate.contentView
import javax.inject.Inject

class IntrudersPhotosActivity : BaseActivity<IntrudersPhotosViewModel>() {

    @Inject
    lateinit var intrudersListAdapter: IntrudersListAdapter

    private val binding: ActivityIntrudersPhotosBinding by contentView(R.layout.activity_intruders_photos)

    override fun getViewModel(): Class<IntrudersPhotosViewModel> =
        IntrudersPhotosViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.recyclerViewIntrudersPhotosList.adapter = intrudersListAdapter

        binding.imageViewBack.setOnClickListener { finish() }
       
         binding.clearAllClickListener = View.OnClickListener {
             clearAllPhotos()
         }


        viewModel.getIntruderListViewState().observe(this, Observer {
            intrudersListAdapter.updateIntruderList(it.intruderPhotoItemViewStateList)
            binding.viewState = it
            binding.executePendingBindings()
        })
    }

   private fun clearAllPhotos() {
        viewModel.deleteAllPhotos()
        intrudersListAdapter.clearAllPhotos()
        Toast.makeText(this, "All photos cleared", Toast.LENGTH_SHORT).show()
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, IntrudersPhotosActivity::class.java)
        }
    }
}