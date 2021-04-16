package com.example.testpdu

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*


class BluetoothLeService():Service() {

    private val TAG:String=BluetoothLeService::class.java.simpleName
    private lateinit var manager:BluetoothManager
    private lateinit var adapter:BluetoothAdapter
    private lateinit var mAddress:String
    private var gatt:BluetoothGatt? = null

    companion object{
        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    }


    inner class LocalBinder : Binder() {
        fun getService():BluetoothLeService{
            return this@BluetoothLeService
        } }
    private val mBinder=LocalBinder()
    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    fun initialize():Boolean{
        if (manager==null){
            manager=getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            if (manager==null){
                Log.e(TAG,"unable to initialize manager")
                return false
            }
        }
        adapter=manager.adapter
        if (adapter==null){
            Log.e(TAG,"unable to obtain an adapter")
            return false
        }
        return true
    }


    private val mGattCallback= object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            var intentAction:String
            if (newState==BluetoothProfile.STATE_CONNECTED){
                intentAction= ACTION_GATT_CONNECTED
                broadcastUpdate(intentAction)
                Log.d(TAG,"connect to GATT server")
                Log.d(TAG,"start to service discovery : ${gatt!!.discoverServices()}")

            }else if (newState==BluetoothProfile.STATE_DISCONNECTED){

                intentAction= ACTION_GATT_DISCONNECTED
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status==BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            }else{
                Log.d(TAG," onServicesDsicovered received : $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)

            if (characteristic==null)return

            if (status==BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)

            if (characteristic==null)return

            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic)
        }
    }

    private fun broadcastUpdate(action: String){
        val intent=Intent(action)
        sendBroadcast(intent)
    }
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic){
        val intent=Intent(action)


    }

    fun connect(address:String):Boolean{
        if (adapter==null||address==null){
            return false
        }
        if (mAddress!=null&& address == mAddress &&gatt!=null){
            return gatt!!.connect()
        }

        val device=adapter.getRemoteDevice(address)
        if (device==null){
            return false
        }
        gatt=device.connectGatt(this,false,mGattCallback)
        mAddress=address

        return true
    }
    fun disconnect(){
        if (adapter==null||gatt==null){
            return
        }
        gatt!!.disconnect()

    }

    fun close(){
        if (gatt==null){
            return
        }
        gatt!!.close()
        gatt = null
    }

    fun readCharacterstic(characteristic: BluetoothGattCharacteristic){
        if (adapter==null||gatt==null){
            return
        }
        gatt!!.readCharacteristic(characteristic)
    }

    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,enabled:Boolean){
        if (adapter==null||gatt==null){
            return
        }
        gatt!!.setCharacteristicNotification(characteristic,enabled)
        if (GattAttributes.current==characteristic.uuid.toString()){
            val descriptor=characteristic.getDescriptor(UUID.fromString(GattAttributes.CONFIG))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt!!.writeDescriptor(descriptor)

        }
    }

}