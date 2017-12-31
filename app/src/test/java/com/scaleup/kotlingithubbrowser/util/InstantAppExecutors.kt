package com.scaleup.kotlingithubbrowser.util

import com.scaleup.kotlingithubbrowser.AppExecutors
import java.util.concurrent.Executor


class InstantAppExecutors : AppExecutors(instant, instant, instant) {
    companion object {
        private val instant = Executor{ command -> command.run() }
    }
}
