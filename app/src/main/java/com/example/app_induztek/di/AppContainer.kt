package com.example.app_induztek.di

import android.content.Context
import com.example.app_induztek.BuildConfig
import com.example.app_induztek.data.local.AppDatabase
import com.example.app_induztek.data.remote.RemoteDataSource
import com.example.app_induztek.data.remote.RetrofitClient
import com.example.app_induztek.data.remote.TokenStore
import com.example.app_induztek.data.repository.InduztekRepository

class AppContainer(context: Context) {
    val database = AppDatabase.getDatabase(context)
    val tokenStore = TokenStore(context)
    val api = RetrofitClient.create(BuildConfig.API_BASE_URL, BuildConfig.DEBUG, tokenStore)
    val remote = RemoteDataSource(api)
    val repository = InduztekRepository(database, remote, tokenStore)
    val session = SessionManager(tokenStore)
    val messages = UserMessages()
}
