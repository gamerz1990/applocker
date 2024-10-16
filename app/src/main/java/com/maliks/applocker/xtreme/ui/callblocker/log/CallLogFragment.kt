package com.maliks.applocker.xtreme.ui.callblocker.log

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.maliks.applocker.xtreme.R
import com.maliks.applocker.xtreme.databinding.FragmentCallBlockerLogsBinding
import com.maliks.applocker.xtreme.ui.BaseFragment
import com.maliks.applocker.xtreme.ui.callblocker.blacklist.BlackListAdapter
import com.maliks.applocker.xtreme.util.delegate.inflate

class CallLogFragment : BaseFragment<CallLogViewModel>() {

    private val binding: FragmentCallBlockerLogsBinding by inflate(R.layout.fragment_call_blocker_logs)

    private val callLogsAdapter = CallLogAdapter()

    override fun getViewModel(): Class<CallLogViewModel> = CallLogViewModel::class.java

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding.recyclerViewCallLogs.adapter = callLogsAdapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getViewStateLiveData().observe(this, Observer {
            binding.viewState = it
            binding.executePendingBindings()
            callLogsAdapter.setCallLogsItems(it.callLogsViewState)
        })
    }

    companion object {
        fun newInstance() = CallLogFragment()
    }

}