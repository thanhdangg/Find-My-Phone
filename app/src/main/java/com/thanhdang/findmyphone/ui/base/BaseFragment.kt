package com.thanhdang.findmyphone.ui.base

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.thanhdang.findmyphone.R

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    protected abstract fun getBindingInflater(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean
    ): VB

    protected abstract fun initData(bundle: Bundle?)
    protected abstract fun initViews()
    protected abstract fun initActions()

    private var _binding: VB? = null
    val binding get() = _binding!!

    var mNavController: NavController? = null
    val handler = Handler(Looper.getMainLooper())

    private var backPressedTime: Long = 0

    fun Fragment.safeFindNavController(): NavController? {
        return try {
            findNavController()
        } catch (e: IllegalStateException) {
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = getBindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mNavController = safeFindNavController()

        initData(arguments)
        initViews()
        initActions()
    }

    fun navigateFromCurrentGraph(desNavId: Int, data: Bundle?) {
        mNavController?.navigate(desNavId, data, navigatorOption())
    }

    fun popBackStackNavigate() {
        mNavController?.popBackStack()
    }

    fun popBackStackNavigate(@IdRes destinationId: Int, inclusive: Boolean) {
        mNavController?.popBackStack(destinationId = destinationId, inclusive = inclusive)
    }

    fun checkBackstackToFinishActivityOrPop() {
        if (isFinalInBackStack()) {
            requireActivity().finish()
        } else{
            popBackStackNavigate()
        }
    }

    fun exitActivity() {
        val isFinalFragment = isFinalInBackStack()

        if (isFinalFragment) {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                requireActivity().finishAffinity()
            } else {
                Toast.makeText(
                    requireActivity(), resources.getString(R.string.tap_exit), Toast.LENGTH_LONG
                ).show()
            }
            backPressedTime = System.currentTimeMillis()
        } else {
            mNavController?.popBackStack()
        }
    }

    fun isFinalInBackStack(): Boolean {
        val previousDestination = mNavController?.previousBackStackEntry?.destination

        return previousDestination == null
    }


    fun navigatorOption(): NavOptions {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)

        return navOptions.build()
    }
}