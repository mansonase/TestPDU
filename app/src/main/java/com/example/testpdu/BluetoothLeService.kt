package com.example.testpdu

import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and


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

                /*
                Handler(Looper.getMainLooper()).postDelayed(
                    Runnable {
                        Log.d(TAG,"start to service discovery : ${gatt!!.discoverServices()}")
                    },600
                )

                 */

            }else if (newState==BluetoothProfile.STATE_DISCONNECTED){

                intentAction= ACTION_GATT_DISCONNECTED
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            Log.d(TAG,"in onServicesDiscovered, $status")
            if (status==BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                Log.d(TAG," on services discovered, after start to service discovery")
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
                Log.d(TAG,"read successfully, ${characteristic.getStringValue(0)}")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)

            if (characteristic==null){
                Log.d(TAG,"characteristic is....null!!!")
                return
            }

            Log.d(TAG,"characteristic changed")
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic)
        }
    }

    private fun broadcastUpdate(action: String){
        val intent=Intent(action)
        sendBroadcast(intent)
    }
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic){
        val intent=Intent(action)

        Log.d(TAG,characteristic.uuid.toString())
        when(characteristic.uuid.toString()){

            GattAttributes.device_name->{
                val data= characteristic.getStringValue(0)
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mDeviceName)
            }
            GattAttributes.appearance->{
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mAppearance)
                Log.d(TAG, "$data, and size is $size")
            }
            GattAttributes.peripheral_preferred_connection_parameters->{
//16,0,60,0,0,0,-112,1
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mPeripheralParameters)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.device_id->{
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mDeviceId)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.manufacturer_name_string->{
                val data=characteristic.getStringValue(0)
                val size=characteristic.value.size
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mManufacturerNameString)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.model_number_string->{
                val data=characteristic.getStringValue(0)
                val size=characteristic.value.size
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mModelNumberString)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.serial_number_string->{
                val data=characteristic.getStringValue(0)
                val size=characteristic.value.size
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSerialNumberString)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.firmware_revision_string->{
                val data=characteristic.getStringValue(0)
                val size=characteristic.value.size
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mFirmwareRevision)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.hardware_revision_string->{
                val data=characteristic.getStringValue(0)
                val size=characteristic.value.size
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mHardwareRevision)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.software_revision_string->{
                val data=characteristic.getStringValue(0)
                val size=characteristic.value.size
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSoftwareRevision)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.current->{

                val data = (characteristic.value[0].toInt()+(characteristic.value[1].toInt()*256)+(characteristic.value[2].toInt()*65535)).toString()

                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mCurrent)
                Log.d(TAG, "$data, current")
            }
            GattAttributes.voltage->{
                val data = getNoMoreThanTwoDigits(((characteristic.value[0].toDouble()+(characteristic.value[1].toDouble()*256)+(characteristic.value[2].toDouble()*65535))/1000))

                Log.d(TAG, "$data, voltage")

                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mVoltage)

            }
            GattAttributes.watt->{
                val data = getNoMoreThanTwoDigits (((characteristic.value[0].toDouble()+(characteristic.value[1].toDouble()*256)+(characteristic.value[2].toDouble()*65535))/1000))

                Log.d(TAG, "$data, watt")
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mWatt)
            }
            GattAttributes.power_factor->{
                val data = getNoMoreThanTwoDigits (((characteristic.value[0].toDouble()+(characteristic.value[1].toDouble()*256))/1000))

                Log.d(TAG, "$data, power factor")
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mPowerFactor)
            }
            GattAttributes.load->{
                val data=(characteristic.value[0].toInt()).toString()
                Log.d(TAG, "$data, load")
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mLoad)
            }
            GattAttributes.load_detected->{
                val data=(characteristic.value[0].toInt()).toString()
                Log.d(TAG, "$data, load detected")
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mLoadDetected)
            }
            GattAttributes.activate_power->{
                val data=characteristic.value[0].toInt().toString()
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mActivatePower)
                Log.d(TAG, "$data, activate power")
            }
            GattAttributes.set_time->{
                val data = (characteristic.value[0].toLong()
                        +(characteristic.value[1].toLong()*256)
                        +(characteristic.value[2].toLong()*65535)
                        +(characteristic.value[3].toLong()*256*65535))

                val time=getDate(data)
                intent.putExtra(EXTRA_DATA, time)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSetTime)
                Log.d(TAG, time)
            }
            GattAttributes.download_request->{
                val data=characteristic.value
                intent.putExtra(EXTRA_DATA, String(data))
                intent.putExtra(CHARACTERISTIC,GattAttributes.mDownloadRequest)
                Log.d(TAG, String(data))
            }
            GattAttributes.read_recorded_data->{
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mReadRecordedData)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.nfc_tag_id->{
                var data=""
                val size=characteristic.value.size
                for (i in 0 until size){
                    data+= characteristic.value[i].toChar()
                }
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mNfcTagId)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.charging_latency->{
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mChargingLatency)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.hardware_status->{
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mHardwareStatus)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.software_status->{
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mSoftwareStatus)
                Log.d(TAG, "$data, size is $size")
            }
            GattAttributes.error_code->{
                var data="["
                val size=characteristic.value.size
                for (i in 0 until size){
                    data += if (i==size-1) {
                        characteristic.value[i].toString()
                    }else{
                        characteristic.value[i].toString() + ","
                    }
                }
                data += "]"
                intent.putExtra(EXTRA_DATA, data)
                intent.putExtra(CHARACTERISTIC,GattAttributes.mErrorCode)
                Log.d(TAG, "$data, size is $size")
            }
        }
        sendBroadcast(intent)
    }

    fun connect(address:String):Boolean{
        if (adapter==null||address==null){
            return false
        }
        if (mAddress!=null&& address == mAddress &&gatt!=null){
            Log.d(TAG,"trying to use an existing gatt for connection, $mAddress")
            return gatt!!.connect()
        }

        val device=adapter!!.getRemoteDevice(address)
        if (device==null){
            Log.d(TAG," no devices")
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

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic){
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
        if (characteristic.uuid!=null){
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
    fun getSupportedGattService(strUUID: String): BluetoothGattService? {
        if (gatt==null)return null

        return gatt!!.getService(UUID.fromString(strUUID))
    }
    private fun getNoMoreThanTwoDigits(number:Double):String{
        val format=DecimalFormat("0.##")
        format.roundingMode=RoundingMode.FLOOR
        return format.format(number)
    }

    fun getDate(seconds :Long):String{
        val formatter=SimpleDateFormat("dd/MM/yyyy\nHH:mm:ss")

        val calendar=Calendar.getInstance()
        calendar.timeInMillis=seconds*1000
        return formatter.format(calendar.time)
    }
}