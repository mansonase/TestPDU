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
    private var manager:BluetoothManager?=null
    private var adapter:BluetoothAdapter?=null
    private var mAddress:String?=null
    private var gatt:BluetoothGatt? = null

    companion object{
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

        const val CHARACTERISTIC="com.example.bluetooth.le.characteristic"
    }


    inner class LocalBinder : Binder() {
        fun getService():BluetoothLeService{
            return this@BluetoothLeService
        } }
    private val mBinder=LocalBinder()
    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    fun initialize():Boolean{
        if (manager==null){
            manager=getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            if (manager==null){
                Log.e(TAG,"unable to initialize manager")
                return false
            }
        }
        adapter=manager!!.adapter
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
                Log.d(TAG," onServicesDiscovered received : $status")
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

        when(characteristic.uuid.toString()){

            GattAttributes.device_name->{
                val data= characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mDeviceName)
                Log.d(TAG, String(data))
            }
            GattAttributes.appearance->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mAppearance)
                Log.d(TAG, String(data))
            }
            GattAttributes.peripheral_preferred_connection_parameters->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mPeripheralParameters)
                Log.d(TAG, String(data))
            }
            GattAttributes.device_id->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mDeviceId)
                Log.d(TAG, String(data))
            }
            GattAttributes.model_number_string->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mModelNumberString)
                Log.d(TAG, String(data))
            }
            GattAttributes.serial_number_string->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSerialNumberString)
                Log.d(TAG, String(data))
            }
            GattAttributes.firmware_revision_string->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mFirmwareRevision)
                Log.d(TAG, String(data))
            }
            GattAttributes.hardware_revision_string->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mHardwareRevision)
                Log.d(TAG, String(data))
            }
            GattAttributes.software_revision_string->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSoftwareRevision)
                Log.d(TAG, String(data))
            }
            GattAttributes.current->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mCurrent)
                Log.d(TAG, String(data))
            }
            GattAttributes.voltage->{
                val data=characteristic.value
                Log.d(TAG, String(data))
            }
            GattAttributes.watt->{
                val data=characteristic.value
                Log.d(TAG, String(data))
            }
            GattAttributes.power_factor->{
                val data=characteristic.value
                Log.d(TAG, String(data))
            }
            GattAttributes.load->{
                val data=characteristic.value
                Log.d(TAG, String(data))
            }
            GattAttributes.load_detected->{
                val data=characteristic.value
                Log.d(TAG, String(data))
            }
            GattAttributes.activate_power->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mActivatePower)
                Log.d(TAG, String(data))
            }
            GattAttributes.set_time->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSetTime)
                Log.d(TAG, String(data))
            }
            GattAttributes.download_request->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mDownloadRequest)
                Log.d(TAG, String(data))
            }
            GattAttributes.read_recorded_data->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mReadRecordedData)
                Log.d(TAG, String(data))
            }
            GattAttributes.nfc_tag_id->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mNfcTagId)
                Log.d(TAG, String(data))
            }
            GattAttributes.charging_latency->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mChargingLatency)
                Log.d(TAG,String(data))
            }
            GattAttributes.hardware_status->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC, GattAttributes.mHardwareStatus)
                Log.d(TAG, String(data))
            }
            GattAttributes.software_status->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSoftwareStatus)
                Log.d(TAG, String(data))
            }
            GattAttributes.error_code->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mErrorCode)
            }
        }
        sendBroadcast(intent)
    }

    fun connect(address:String):Boolean{
        if (adapter==null||address==null){
            return false
        }
        if (mAddress!=null&& address == mAddress &&gatt!=null){
            Log.d(TAG,"trying to use an existing gatt for connection")
            return gatt!!.connect()
        }

        val device=adapter!!.getRemoteDevice(address)
        if (device==null){
            return false
        }
        Log.d(TAG,"finally , we need to connect here ");
        gatt=device.connectGatt(this,false,mGattCallback)
        mAddress=address

        Log.d(TAG,"$mAddress, connect")
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

    fun getSupportedGattServices():List<BluetoothGattService>?{
        if (gatt==null)
            return null

        return gatt!!.services
    }
}