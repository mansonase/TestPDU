package com.example.testpdu

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList

class ContentActivity : AppCompatActivity(),View.OnClickListener {

    val TAG= ContentActivity::class.java.simpleName


    private lateinit var mDeviceName:String
    private lateinit var mDeviceAddress:String
    private var mGattServiceList:List<BluetoothGattService>?=null

    private var mBluetoothLeService: BluetoothLeService? = null
    private var isConnected=false

    private var characteristic:BluetoothGattCharacteristic?=null

    private lateinit var tvDeviceName:TextView
    private lateinit var tvAppearance:TextView
    private lateinit var tvPeripheralParameters:TextView
    private lateinit var tvDeviceId:TextView
    private lateinit var tvModelNumber:TextView
    private lateinit var tvSerialNumber:TextView
    private lateinit var tvFirmwareRevision:TextView
    private lateinit var tvHardwareRevision:TextView
    private lateinit var tvSoftwareRevision:TextView
    private lateinit var tvManufacturerName:TextView
    private lateinit var tvCurrent:TextView
    private lateinit var tvVoltage:TextView
    private lateinit var tvWatt:TextView
    private lateinit var tvPowerFactor:TextView
    private lateinit var tvLoad:TextView
    private lateinit var tvLoadDetected:TextView
    private lateinit var tvPowerOn:TextView
    private lateinit var tvPowerOff:TextView
    private lateinit var tvSetTime:TextView
    private lateinit var tvGetTime:TextView
    private lateinit var tvDownloadOn:TextView
    private lateinit var tvRDStartTime:TextView
    private lateinit var tvRDEndTime:TextView
    private lateinit var tvRDCurrent:TextView
    private lateinit var tvRDVoltage:TextView
    private lateinit var tvRDPower:TextView
    private lateinit var tvRDPowerFactor:TextView
    private lateinit var tvRDConsumption:TextView
    private lateinit var tvRDNone:TextView
    private lateinit var tvNfcTagId:TextView
    private lateinit var tvChargingLatency:TextView
    private lateinit var tvHardwareStatus:TextView
    private lateinit var tvSoftwareStatus:TextView
    private lateinit var tvErrorCode:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect_activity)
        val intent=intent
        mDeviceName= intent.getStringExtra(GattAttributes.DEVICE_NAME).toString()
        mDeviceAddress=intent.getStringExtra(GattAttributes.DEVICE_ADDRESS).toString()

        tvDeviceName=findViewById(R.id.device_name_content)
        tvAppearance=findViewById(R.id.appearance_content)
        tvPeripheralParameters=findViewById(R.id.peripheral_pcp_content)
        tvDeviceId=findViewById(R.id.device_id_content)
        tvModelNumber=findViewById(R.id.model_number_content)
        tvSerialNumber=findViewById(R.id.serial_number_content)
        tvFirmwareRevision=findViewById(R.id.firmware_version_content)
        tvHardwareRevision=findViewById(R.id.hardware_version_content)
        tvSoftwareRevision=findViewById(R.id.software_version_content)
        tvManufacturerName=findViewById(R.id.manufacturer_name_content)
        tvCurrent=findViewById(R.id.current_content)
        tvVoltage=findViewById(R.id.voltage_content)
        tvWatt=findViewById(R.id.watt_content)
        tvPowerFactor=findViewById(R.id.power_factor_content)
        tvLoad=findViewById(R.id.load_content)
        tvLoadDetected=findViewById(R.id.load_detected_content)
        tvPowerOn=findViewById(R.id.power_on_content)
        tvPowerOff=findViewById(R.id.power_off_content)
        tvSetTime=findViewById(R.id.set_time_content)
        tvGetTime=findViewById(R.id.get_time_content)
        tvDownloadOn=findViewById(R.id.download_on_content)

        tvRDStartTime=findViewById(R.id.recorded_data_start_time_content)
        tvRDEndTime=findViewById(R.id.recorded_data_end_time_content)
        tvRDCurrent=findViewById(R.id.recorded_data_current_content)
        tvRDVoltage=findViewById(R.id.recorded_data_voltage_content)
        tvRDPower=findViewById(R.id.recorded_data_power_content)
        tvRDPowerFactor=findViewById(R.id.recorded_data_power_factor_content)
        tvRDConsumption=findViewById(R.id.recorded_data_consumption_content)
        tvRDNone=findViewById(R.id.recorded_data_none_content)

        tvNfcTagId=findViewById(R.id.nfc_tag_id_content)
        tvChargingLatency=findViewById(R.id.charging_latency_content)
        tvHardwareStatus=findViewById(R.id.hardware_status_content)
        tvSoftwareStatus=findViewById(R.id.software_status_content)
        tvErrorCode=findViewById(R.id.error_code_content)



        actionBar?.title=mDeviceName
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val serviceIntent=Intent(this,BluetoothLeService::class.java)
        bindService(serviceIntent,mServiceConnection, BIND_AUTO_CREATE)

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter())
        val result=mBluetoothLeService?.connect(mDeviceAddress)
        Log.d(TAG," Connect request result : $result")

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.gatt_services,menu)
        if (isConnected){
            if (menu != null) {
                menu.findItem(R.id.menu_connect).isVisible = false
                menu.findItem(R.id.menu_disconnect).isVisible=true
            }
        }else{
            if (menu!=null){
                menu.findItem(R.id.menu_connect).isVisible=true
                menu.findItem(R.id.menu_disconnect).isVisible=false
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_connect->{
                mBluetoothLeService?.connect(mDeviceAddress)
                return true
            }
            R.id.menu_disconnect->{
                mBluetoothLeService?.disconnect()
                return true
            }
            android.R.id.home->{
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private val mServiceConnection= object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder?) {
            mBluetoothLeService=(service as BluetoothLeService.LocalBinder).getService()
            if (mBluetoothLeService!=null){
                if (!mBluetoothLeService!!.initialize()){
                    finish()
                }
                mBluetoothLeService!!.connect(mDeviceAddress)
                Log.d(TAG,"in service connected")
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName?) {
            mBluetoothLeService=null
            Log.d(TAG,"in service disconnected")
        }
    }

    private val mGattUpdateReceiver= object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            val action=p1?.action

            if (BluetoothLeService.ACTION_GATT_CONNECTED==action){

                Log.d(TAG,"hihihi, connected")
            }else if (BluetoothLeService.ACTION_GATT_DISCONNECTED==action){
                isConnected=false
                invalidateOptionsMenu()
            }else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED==action){

                if (mBluetoothLeService==null){
                    mGattServiceList=null
                }else {
                    isConnected=true
                    invalidateOptionsMenu()
                }
                Log.d(TAG,"in contentActivity and we got discovered")
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE==action){
                displayData(p1)
                Log.d(TAG,"displaydata-ing")
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


    private fun displayData(intent: Intent){

        when(intent.getStringExtra(BluetoothLeService.CHARACTERISTIC)){

            GattAttributes.mDeviceName->{

                tvDeviceName.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mAppearance->{
                tvAppearance.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mPeripheralParameters->{
                tvPeripheralParameters.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mDeviceId->{
                tvDeviceId.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mModelNumberString->{
                tvModelNumber.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mSerialNumberString->{
                tvSerialNumber.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mFirmwareRevision->{
                tvFirmwareRevision.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mHardwareRevision->{
                tvHardwareRevision.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mSoftwareRevision->{
                tvSoftwareRevision.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mManufacturerNameString->{
                tvManufacturerName.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mCurrent->{
                tvCurrent.text=(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)+" mA")
            }
            GattAttributes.mVoltage->{
                tvVoltage.text=(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)+" V")
            }
            GattAttributes.mWatt->{
                tvWatt.text=(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)+" W")
            }
            GattAttributes.mPowerFactor->{
                tvPowerFactor.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mLoad->{
                tvLoad.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mLoadDetected->{
                tvLoadDetected.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mPowerOn->{
                tvPowerOn.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                tvPowerOff.text="False"
            }
            GattAttributes.mPowerOff->{
                tvPowerOff.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                tvPowerOn.text="False"
            }
            GattAttributes.mSetTime -> {
                tvSetTime.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                tvGetTime.text="False"
            }
            GattAttributes.mGetTime->{
                tvGetTime.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                tvSetTime.text="False"
            }
            GattAttributes.mDownloadOn->{
                tvDownloadOn.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mReadRecordedData -> {
                
                val array=intent.getStringArrayListExtra(BluetoothLeService.EXTRA_DATA)
                if (array != null) {
                    tvRDStartTime.text=array.get(0)
                    tvRDEndTime.text=array.get(1)
                    tvRDCurrent.text=array.get(2)
                    tvRDVoltage.text=array.get(3)
                    tvRDPower.text=array.get(4)
                    tvRDPowerFactor.text=array.get(5)
                    tvRDConsumption.text=array.get(6)
                    tvRDNone.text=array.get(7)
                }
            }
            GattAttributes.mNfcTagId->{
                tvNfcTagId.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mChargingLatency -> {

                val hours=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)

                when(hours){

                    "0","1"->{
                        tvChargingLatency.text=("$hours hour")
                    }
                    else->{
                        tvChargingLatency.text=("$hours hours")
                    }

                }
            }
            GattAttributes.mHardwareStatus->{
                tvHardwareStatus.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mSoftwareStatus->{
                tvSoftwareStatus.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mErrorCode->{
                tvErrorCode.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }

        }
    }

    override fun onClick(v: View?) {
        if (!isConnected)return

        when(v?.id){

            R.id.device_name_title->{
                Log.d(TAG,"device name starting!!!")
                characteristic=(mBluetoothLeService!!.getSupportedGattService(GattAttributes.GENERAL_ACCESS)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.device_name))
                if (characteristic!=null) {
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }

            }
            R.id.appearance_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.GENERAL_ACCESS)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.appearance))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.peripheral_pcp_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.GENERAL_ACCESS)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.peripheral_preferred_connection_parameters))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.current_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.current))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            R.id.voltage_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.voltage))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            R.id.watt_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.watt))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            R.id.power_factor_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.power_factor))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            R.id.load_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.load))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            R.id.load_detected_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.load_detected))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            R.id.power_on_title -> {

                val byteArray= byteArrayOf((0x01).toByte())
                characteristic =
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.activate_power))
                if (characteristic != null) {
                    //mBluetoothLeService?.readCharacteristic(characteristic!!)
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
                Log.d(TAG," power on")
            }
            R.id.power_off_title -> {

                val byteArray= byteArrayOf(0x00)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.activate_power))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
                Log.d(TAG," power off")
            }
            R.id.set_time_title -> {

                val epochTime=Calendar.getInstance().timeInMillis/1000
                val timeIn16=epochTime.toString(16)

                Log.d(TAG,"time is : $timeIn16")

                val hex1=timeIn16[6].toString()+timeIn16[7].toString()
                val hex2=timeIn16[4].toString()+timeIn16[5].toString()
                val hex3=timeIn16[2].toString()+timeIn16[3].toString()
                val hex4=timeIn16[0].toString()+timeIn16[1].toString()

                Log.d(TAG," $hex1,$hex2,$hex3,$hex4")

                val byteArray= byteArrayOf(hex1.toInt(16).toByte(),hex2.toInt(16).toByte(),hex3.toInt(16).toByte(),hex4.toInt(16).toByte())

                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.set_time))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }

            }
            R.id.get_time_title -> {
                characteristic =
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.set_time))
                if (characteristic != null) {
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.download_on_title -> {

                val byteArray= byteArrayOf(0x01)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.download_request))

                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }

            }
            R.id.recorded_data_button->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.read_recorded_data))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.nfc_tag_id_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.nfc_tag_id))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.charging_latency_0->{
                val byteArray= byteArrayOf(0x00)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.charging_latency))

                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            R.id.charging_latency_1->{
                val byteArray= byteArrayOf(0x01)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.charging_latency))

                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            R.id.charging_latency_2->{
                val byteArray= byteArrayOf(0x02)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.charging_latency))

                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            R.id.charging_latency_3->{
                val byteArray= byteArrayOf(0x03)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.charging_latency))

                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            R.id.charging_latency_4->{
                val byteArray= byteArrayOf(0x04)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.charging_latency))

                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            R.id.charging_latency_5->{
                val byteArray= byteArrayOf(0x05)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.charging_latency))

                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            R.id.device_id_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_INFORMATION)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.device_id))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.manufacturer_name_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_INFORMATION)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.manufacturer_name_string))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.model_number_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_INFORMATION)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.model_number_string))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.serial_number_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_INFORMATION)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.serial_number_string))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.firmware_version_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_INFORMATION)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.firmware_revision_string))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.hardware_version_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_INFORMATION)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.hardware_revision_string))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.software_version_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_INFORMATION)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.software_revision_string))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.hardware_status_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.SYSTEM_STATUS)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.hardware_status))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.software_status_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.SYSTEM_STATUS)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.software_status))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.error_code_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.SYSTEM_STATUS)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.error_code))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
        }
    }
}