package com.nearby.messages.nearbyconnection.ui.connection

import android.content.Context
import com.nearby.messages.nearbyconnection.arch.BaseMvp

interface ConnectionMvp : BaseMvp {
    interface View : BaseMvp.View {
//        fun showAvaibleDevicesDialog(avaibleGuests: HashMap<String, String>)
//        fun updateConnectionList(availableRooms: MutableList<String>)
    }

    interface Presenter : BaseMvp.Presenter {
//        fun init()
//        fun stopDiscovery()
//        fun stopAdvertising()
//        fun startDiscovery(packageName: String)
//        fun startAdvertising(username: String, packageName: String)
//        fun stopAllConnections()
//        fun acceptConnection(user: String, endpointId: String)
//        fun getAvaibleGuests(): HashMap<String, String>
//        fun requestConnection(endpointId: String)
    }
}