package com.seoultech.ecgmonitor.bluetooth

import androidx.lifecycle.LiveData

abstract class BluetoothStateLiveData : LiveData<Boolean>(), BluetoothStateObservable