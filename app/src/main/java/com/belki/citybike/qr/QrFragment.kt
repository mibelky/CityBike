package com.belki.citybike.qr

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.belki.citybike.*
import com.belki.citybike.databinding.FragmentQrBinding
import com.belki.citybike.utils.cameraPermissionApproved
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback


class QrFragment : Fragment() {
    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: FragmentQrBinding
    private val viewModel: CityBikeViewModel by activityViewModels()
    private val tag = "QrFragment"
//    private var windowInsetsController: WindowInsetsControllerCompat? = null

    private val permissionRequester = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            isGranted -> {
                activateScanner()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            ) -> {
                Log.d(tag, "Should show request rationale")
                binding.button.setOnClickListener {
                    requestPermission()
                }
                binding.cameraBanner.visibility = View.VISIBLE
//                viewModel.showBanner("Application has no camera permission", "Permit")
//                {
//                    Log.d(tag, "Banner button with requirePermission() clicked")
//
//                }
            }
            else -> {
                Log.d(tag, "Don't ask again -> Banner with startActivity")
                binding.button.setOnClickListener {
                    startActivity(
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                }
                binding.cameraBanner.visibility = View.VISIBLE
//                viewModel.showBanner("Application has no camera permission", "Permit")
//                {
//                    Log.d(tag, "Banner button with startActivity() clicked")
//                    startActivity(
//                        Intent().apply {
//                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
//                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                        })
//
//                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQrBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = this
        val activity = requireActivity()

        viewModel.leaveQrEvent.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigate(R.id.action_qrFragment_to_mapFragment)
                viewModel.finishLeaveQrEvent()
            }
        }

        binding.closeImageButton.setOnClickListener {
            viewModel.startLeaveQrEvent()
        }

        codeScanner = CodeScanner(activity, binding.scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                viewModel.startLeaveQrEvent()
            }
        }

        requestPermission()
    }

    private fun requestPermission() {
        permissionRequester.launch(Manifest.permission.CAMERA)
    }

    private fun activateScanner() {
        codeScanner.startPreview()
    }

    override fun onResume() {
        super.onResume()
        if (cameraPermissionApproved()) {
            binding.cameraBanner.visibility = View.GONE
            if (::codeScanner.isInitialized) codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) codeScanner.releaseResources()
        super.onPause()
    }
}