package com.mapbox.navigation.instrumentation_tests.utils.idling

import androidx.test.espresso.IdlingResource
import com.mapbox.navigation.testing.ui.idling.NavigationIdlingResource
import com.mapbox.navigation.ui.NavigationView

/**
 * Becomes idle when [NavigationView] is initialized.
 */
class NavigationViewInitIdlingResource(
    private val navigationView: NavigationView
) : NavigationIdlingResource() {

    private var initialized = false

    override fun getName() = "NavigationViewInitIdlingResource"

    override fun isIdleNow() = initialized

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        navigationView.initialize {
            initialized = true
            callback?.onTransitionToIdle()
        }
    }
}
