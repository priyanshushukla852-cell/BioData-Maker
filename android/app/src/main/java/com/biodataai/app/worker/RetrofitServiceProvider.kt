package com.biodataai.app.worker

import android.content.Context
import com.biodataai.app.network.RetrofitClient
import com.biodataai.app.network.api.BiodataService

object RetrofitServiceProvider {
    private var biodataService: BiodataService? = null

    fun initialize(context: Context) {
        if (biodataService == null) {
            val retrofit = RetrofitClient.getRetrofit(context)
            biodataService = retrofit.create(BiodataService::class.java)
        }
    }

    fun getBiodataService(): BiodataService {
        return biodataService ?: throw IllegalStateException("RetrofitServiceProvider not initialized")
    }
}
