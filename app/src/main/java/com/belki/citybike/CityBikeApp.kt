package com.belki.citybike

import android.app.Application
import com.belki.citybike.local.LocalDB
import com.belki.citybike.local.StationDataSource
import com.belki.citybike.local.StationsLocalRepository
import com.belki.citybike.remote.CityBikeApi
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class CityBikeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            /** This ViewModel will be shared between Activity and their fragments */
            viewModel {
                CityBikeViewModel(
                    get(),
                    get() as StationsLocalRepository
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single { StationsLocalRepository(LocalDB.createStationsDao(this@CityBikeApp), CityBikeApi) }

        }

        startKoin {
            androidContext(this@CityBikeApp)
            modules(listOf(myModule))
        }
    }
}