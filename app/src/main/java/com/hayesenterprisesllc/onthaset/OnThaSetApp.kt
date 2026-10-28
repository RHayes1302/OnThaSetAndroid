package com.hayesenterprisesllc.onthaset

import android.app.Application
import com.hayesenterprisesllc.onthaset.billing.BillingManager
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.Prefs
import com.hayesenterprisesllc.onthaset.location.LocationManager

/** App entry point (OnThaSetApp in OnThaSet__New_App.swift). */
class OnThaSetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        LocalStore.init(this)
        LocationManager.init(this)
        AuthService.restoreSession()
        BillingManager.init(this)
    }
}
