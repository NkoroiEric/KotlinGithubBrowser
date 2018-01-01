package com.scaleup.kotlingithubbrowser.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder{
       /*
        Marks a method on a component builder or
        *subcomponent builder that allows an instance to be bound
         *to some type within the component ie @BindsInstance
         */
        @BindsInstance fun inject(application: Application) :Builder
        fun build():AppComponent
    }
}