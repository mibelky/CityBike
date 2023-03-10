package com.belki.citybike.nearest

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment

import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import androidx.navigation.ui.NavigationUI
import com.belki.citybike.CityBikeViewModel
import com.belki.citybike.R
import com.belki.citybike.utils.bindImage
import com.belki.citybike.databinding.FragmentNearestBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class NearestFragment : Fragment() {
    private val viewModel by activityViewModel<CityBikeViewModel>()
    private lateinit var binding: FragmentNearestBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNearestBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.topAppBar.setNavigationOnClickListener{
            binding.drawerLayout.open()
        }

        val navController = findNavController()

        NavigationUI.setupWithNavController(binding.navigationView,navController)
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            binding.drawerLayout.close()
            true
        }

        val profileImage = binding.navigationView.getHeaderView(0).findViewById<ImageView>(R.id.profile_image)
        bindImage(profileImage, getString(R.string.profileErrorPhotoUrl))

        val adapter = StationListAdapter(StationListAdapter.StationClickListener { station ->
            findNavController().navigate(R.id.action_nearestFragment_to_mapFragment)
            viewModel.updateSelectedStation(station.id)
            viewModel.expandBottomSheet()
        })

        binding.stationRecycler.adapter = adapter

        return binding.root
    }
    override fun onResume() {
        Log.d("NearestFragment", "is resumed")
        super.onResume()
    }

    override fun onPause() {
        Log.d("NearestFragment", "is paused")
        super.onPause()
    }

    override fun onStart() {
        Log.d("NearestFragment", "is started")
        super.onStart()
    }

    override fun onStop() {
        Log.d("NearestFragment", "is stopped")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("NearestFragment", "is destroyed")
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}

