package com.scaleup.kotlingithubbrowser.di

import android.app.Application
import com.scaleup.kotlingithubbrowser.GithubApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    MainActivityModule::class,
    ViewModelModule::class
])
interface AppComponent {
    @Component.Builder
    interface Builder{
       /*Marks a method on a component builder or
        *subcomponent builder that allows an instance to be bound
         *to some type within the component ie @BindsInstance
         */
        @BindsInstance fun application(application: Application) :Builder
        fun build():AppComponent
    }
    fun inject(app : GithubApp)
}