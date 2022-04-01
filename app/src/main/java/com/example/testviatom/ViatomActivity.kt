package com.example.testviatom

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.viatom_main.*
import java.util.*
import kotlin.experimental.inv

class ViatomActivity:AppCompatActivity(),View.OnClickListener {

    private lateinit var mDeviceName:String
    private lateinit var mDeviceAddress:String
    private var mBluetoothLeService:BluetoothLeService?=null
    private var characteristic: BluetoothGattCharacteristic?=null
    private var pool:ByteArray?=null

    private val mServiceConnection= object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBluetoothLeService=(service as BluetoothLeService.LocalBinder).getService()
            if (mBluetoothLeService!=null){
                if (!mBluetoothLeService!!.initialize()){
                    finish()
                }
                mBluetoothLeService!!.connect(mDeviceAddress)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBluetoothLeService=null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.viatom_main)
        mDeviceName= intent.getStringExtra(GattAttributes.DEVICE_NAME).toString()
        mDeviceAddress=intent.getStringExtra(GattAttributes.DEVICE_ADDRESS).toString()

        val serviceIntent=Intent(this,BluetoothLeService::class.java)
        bindService(serviceIntent,mServiceConnection, BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter())
        val result=mBluetoothLeService?.connect(mDeviceAddress)
        Log.d("viatommm"," Connect request result : $result")
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService=null
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.get_data->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.BP2_SERVICE) as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.pb2_write))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,Bp2BleCmd.getRtData())
                }
            }
            R.id.activate_bp->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.BP2_SERVICE)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.pb2_write))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,Bp2BleCmd.switchState(0))
                }
            }
        }
    }

    @ExperimentalUnsignedTypes
    private val mGattUpdateReceiver= object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when(intent?.action){
                BluetoothLeService.ACTION_GATT_CONNECTED->{

                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED->{

                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED->{
                    if (mBluetoothLeService!=null){
                        switchOnNotification()
                        able_to_click.text=("able")
                    }
                }
                BluetoothLeService.ACTION_DATA_AVAILABLE->{
                    displayData(intent)
                }
            }
        }
    }

    //@ExperimentalUnsignedTypes
    @ExperimentalUnsignedTypes
    private fun displayData(intent:Intent){
        when(intent.getStringExtra(BluetoothLeService.CHARACTERISTIC)){
            GattAttributes.pb2_write->{

            }
            GattAttributes.pb2_notify->{
                intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA)?.apply {
                    pool= add(pool,this)
                }

                Log.d("viatomTest","   ${pool?.size} ")
                pool?.apply {


                    pool=hasResponse(pool)
                }

            }
        }
    }
    private fun makeGattUpdateIntentFilter():IntentFilter{
        val intentFilter=IntentFilter()
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        return intentFilter
    }

    private fun switchOnNotification(){
        characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.BP2_SERVICE)as BluetoothGattService)
            .getCharacteristic(UUID.fromString(GattAttributes.pb2_notify))
        if (characteristic!=null){
           mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
        }
    }

    @ExperimentalUnsignedTypes
    private fun hasResponse(bytes:ByteArray?):ByteArray?{
        val bytesLeft: ByteArray? = bytes

        if (bytes == null || bytes.size < 8) {
            return bytes
        }

        loop@ for (i in 0 until bytes.size-7) {
            if (bytes[i] != 0xA5.toByte() || bytes[i+1] != bytes[i+2].inv()) {
                continue@loop
            }

            // need content length
            val len = toUInt(bytes.copyOfRange(i+5, i+7))
//            Log.d(TAG, "want bytes length: $len")
            if (i+8+len > bytes.size) {
                continue@loop
            }

            val temp: ByteArray = bytes.copyOfRange(i, i+8+len)


            if (temp.last() == BleCRC.calCRC8(temp)) {
                val bleResponse = UniversalBleResponse.LepuResponse(temp)

                onResponseReceived(bleResponse)

                val tempBytes: ByteArray? = if (i+8+len == bytes.size) null else bytes.copyOfRange(i+8+len, bytes.size)

                return hasResponse(tempBytes)
            }
        }

        return bytesLeft
    }
    @ExperimentalUnsignedTypes
    private fun onResponseReceived(response:UniversalBleResponse.LepuResponse){
        when(response.cmd){
            Bp2BleCmd.RT_DATA->{
                val rtData=Bp2Response.RtData(response.content)
                battery_level_viatom.text=(rtData.param.batteryLevel.toString()+" %")
                voltage_viatom.text=(rtData.param.batteryVol.toString()+" mV")

                val wave=rtData.wave
                wave.dataBpResult?.apply {
                    systolic_viatom.text=("${this.sys} mmHg")
                    diastolic_viatom.text=("${this.dia} mmHg")
                    pulse_viatom.text=("${this.pr} bpm")
                }
            }
            Bp2BleCmd.RT_PARAM->{

            }
            Bp2BleCmd.RT_WAVE->{

            }
        }
    }
}