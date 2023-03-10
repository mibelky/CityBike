package com.belki.citybike.loading

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.belki.citybike.CityBikeViewModel
import com.belki.citybike.R
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class LoadingFragment : Fragment() {
    private val viewModel by activityViewModel<CityBikeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bikeImage = view.findViewById<ImageView>(R.id.bike_image)
        (bikeImage.drawable as AnimatedVectorDrawable).start()

        viewModel.leaveLoadingEvent.observe(viewLifecycleOwner) { shouldLeave ->
            if (shouldLeave) (view as MotionLayout).transitionToEnd {
                findNavController().navigate(R.id.action_loadingFragment_to_mapFragment)
                viewModel.finishLeaveLoadingEvent()
            }
        }
    }
}
/**
 *                  TODO
 *  5) Try to use immersive mode with no system bars for this fragment
 *
 * */