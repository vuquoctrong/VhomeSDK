package com.viettel.vht.sdk.ui.quickhistory

import android.content.res.Configuration
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentHistoryPlaybackQuickBinding
import com.viettel.vht.sdk.utils.custom.MyTabSwitch
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuickPlaybackJFFragment :
    BaseFragment<FragmentHistoryPlaybackQuickBinding, HistoryDetailLiveViewModel>() {
    private val TAG: String = QuickPlaybackJFFragment::class.java.simpleName

    @Inject
    lateinit var rxPreferences: RxPreferences
    private val viewModel: HistoryDetailLiveViewModel by viewModels()

    override val layoutId: Int
        get() = R.layout.fragment_history_playback_quick

    override fun getVM(): HistoryDetailLiveViewModel = viewModel
    private var idCamera: String = ""
    override fun initView() {
        super.initView()
        binding.toolbar.setOnLeftClickListener {
            val fragment = childFragmentManager.fragments.first()
            if (fragment is CardPlaybackJFFragment) {

                if (fragment.checkStopRecord()) return@setOnLeftClickListener
                findNavController().navigateUp()

            } else if (fragment is CloudPlaybackJFFragment) {
                if (fragment.checkStopRecord()) return@setOnLeftClickListener
                findNavController().navigateUp()
            }
        }
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                idCamera = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_VALUE)) {
                if (it.getBoolean(Define.BUNDLE_KEY.PARAM_VALUE)) {
                    binding.tabSwitch.tab2Checked()
                    loadFragment(CloudPlaybackJFFragment())
                } else {
                    binding.tabSwitch.tab1Checked()
                    loadFragment(CardPlaybackJFFragment())
                }
            } else {
                binding.tabSwitch.tab1Checked()
                loadFragment(CardPlaybackJFFragment())
            }
        }
        binding.tabSwitch.addOnTabSwitched(object : MyTabSwitch.OnTabSwitched {
            override fun onChange(isTab1: Boolean) {
                if (isTab1) {
                    binding.tabSwitch.tab2Checked()
                } else {
                    binding.tabSwitch.tab1Checked()
                }
                val fragment = childFragmentManager.fragments.first()
                if (fragment is CardPlaybackJFFragment) {
                    if (fragment.checkStopRecord()) return

                    if (isTab1) {
                        binding.tabSwitch.tab1Checked()
                        arguments?.remove(Define.BUNDLE_KEY.PARAM_START_TIME)
                        loadFragment(CardPlaybackJFFragment())
                    } else {
                        binding.tabSwitch.tab2Checked()
                        loadFragment(CloudPlaybackJFFragment())
                    }
                } else if (fragment is CloudPlaybackJFFragment) {
                    if (fragment.checkStopRecord()) return
                    if (isTab1) {
                        binding.tabSwitch.tab1Checked()
                        arguments?.remove(Define.BUNDLE_KEY.PARAM_START_TIME)
                        loadFragment(CardPlaybackJFFragment())
                    } else {
                        binding.tabSwitch.tab2Checked()
                        loadFragment(CloudPlaybackJFFragment())
                    }
                }
            }
        })
    }

   private fun hideOrShowTop(isHide: Boolean) {
        if (isHide) {
            binding.toolbar.gone()
            binding.tabSwitch.gone()
        } else {
            binding.toolbar.visible()
            binding.tabSwitch.visible()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            hideOrShowTop(true)

        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            hideOrShowTop(false)
        }
    }

    fun loadFragment(fragment: Fragment) {
        // load fragment
        fragment.arguments = arguments
        val transaction: FragmentTransaction =
            childFragmentManager.beginTransaction()
        transaction.replace(R.id.container_quick_history, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}