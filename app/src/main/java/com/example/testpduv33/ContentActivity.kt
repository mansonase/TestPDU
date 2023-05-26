package com.example.testpduv33

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.connect_activity.*
import java.text.SimpleDateFormat
import java.util.*

class ContentActivity : AppCompatActivity(),View.OnClickListener {

    val TAG= ContentActivity::class.java.simpleName


    private lateinit var mDeviceName:String
    private lateinit var mDeviceAddress:String
    private var mGattServiceList:List<BluetoothGattService>?=null

    private var mBluetoothLeService: BluetoothLeService? = null
    private var isConnected=false

    private var characteristic:BluetoothGattCharacteristic?=null


    private val mPlanArray=ByteArray(10)

    private lateinit var progressBar: ProgressBar
    private var minutes=0
    private val reportList:MutableList<MutableMap<String,String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect_activity)
        val intent=intent
        mDeviceName= intent.getStringExtra(GattAttributes.DEVICE_NAME).toString()
        mDeviceAddress=intent.getStringExtra(GattAttributes.DEVICE_ADDRESS).toString()


        charging_latency_show_number.text=("0 ${getString(R.string.minutes)}")
        device_name_title.text=("(0x2A00)\n"+getString(R.string.device_name))
        appearance_title.text=("(0x2A01)\n"+getString(R.string.appearance))
        peripheral_pcp_title.text=("(0x2a04)\n"+getString(R.string.peripheral_parameters))
        power_read.text=("(0xAA21)\n${getString(R.string.read_power)}")
        power_on_title.text=("(0xAA21)\n"+getString(R.string.power_on))
        power_off_title.text=("(0xAA21)\n"+getString(R.string.power_off))
        set_time_title.text=("(0xAA22)\n"+getString(R.string.set_time))
        get_time_title.text=("(0xAA22)\n"+getString(R.string.get_time))
        current_title.text=("(0xAA11) "+getString(R.string.current))
        voltage_title.text=("(0xAA12) "+getString(R.string.Voltage))
        watt_title.text=("(0xAA13) "+getString(R.string.watt))
        power_factor_title.text=("(0xAA14) "+getString(R.string.power_factor))
        recorded_data_button.text=("(0xAA24)\n"+getString(R.string.record_data))

        recorded_data_current_title.text=(getString(R.string.current)+": ")
        recorded_data_voltage_title.text=(getString(R.string.Voltage)+": ")
        recorded_data_power_title.text=(getString(R.string.watt)+": ")
        recorded_data_power_factor_title.text=(getString(R.string.power_factor)+": ")

        load_title.text=("(0xAA15)\n"+getString(R.string.load))
        load_detected_title.text=("(0xAA16)\n"+getString(R.string.load_detect))
        download_on_title.text=("(0xAA23)\n"+getString(R.string.download_on))
        manufacturer_name_title.text=("(0x2A29)\n"+getString(R.string.manufacturer_name))
        nfc_tag_id_title.text=("(0xAA25) "+getString(R.string.nfc_tag_id))
        device_id_title.text=("(0x2A23) "+getString(R.string.device_id))
        model_number_title.text=("(0x2A24)\n"+getString(R.string.model_number))
        serial_number_title.text=("(0x2A25)\n"+getString(R.string.serial_number))
        firmware_version_title.text=("(0x2A26) "+getString(R.string.firmware_version))
        hardware_version_title.text=("(0x2A27) "+getString(R.string.hardware_version))
        software_version_title.text=("(0x2A28) "+getString(R.string.software_version))
        hardware_status_title.text=("(0xAA31)\n"+getString(R.string.hardware_status))
        software_status_title.text=("(0xAA32)\n"+getString(R.string.software_status))
        error_code_title.text=("(0xAA33)\n"+getString(R.string.error_code))
        charging_latency_read_title.text=("(0xAA26)\n"+getString(R.string.charging_latency)+" : ")
        charging_latency_send_title.text=("(0xAA26) "+getString(R.string.send))
        machine_status_title.text=("(0xAA42)\n"+getString(R.string.machine_status))
        meter_version_title.text=("(0xAA43)\n"+getString(R.string.meter_version))

        plan_charging_send.text=("(0xAA27)\n"+"SEND")

        progressBar=findViewById(R.id.progressbar)

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
                set_time_title.performClick()
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE==action){
                displayData(p1)
                Log.d(TAG,"displaydata-ing")
            }else if (BluetoothLeService.ACTION_DATA_WRITE==action){
                displayWriteResponse(p1)
            }
        }
    }

    private fun makeGattUpdateIntentFilter():IntentFilter{
        val intentFilter=IntentFilter()
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE)
        return intentFilter
    }

    private fun displayWriteResponse(intent: Intent){

        when(intent.getStringExtra(BluetoothLeService.CHARACTERISTIC)){
            GattAttributes.mSetTime->{
                set_time_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                get_time_content.text=getString(R.string.fail)
            }
            GattAttributes.mChargingLatencySend->{

                val minutes=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)

                charging_latency_send_content.text=("$minutes mins, ${getString(R.string.msg_sent)}")
                charging_latency_send_content.postDelayed(Runnable {
                    charging_latency_send_content.text=("ready")
                },3000L)
            }
            GattAttributes.mDownloadOn->{
                download_on_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mPowerOn->{
                power_on_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                power_off_content.text=getString(R.string.fail)
                switchOnNotifications()
                //switchOnNewFunction()
            }
            GattAttributes.mPowerOff->{
                power_off_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                power_on_content.text=getString(R.string.fail)
            }
            GattAttributes.mPlanChargingSend->{
                plan_charging_send_result.text=("charging plan sent")
                plan_charging_send_result.postDelayed({
                    plan_charging_send_result.text=("ready")
                },3000L)
            }
        }
    }

    private fun displayData(intent: Intent){

        when(intent.getStringExtra(BluetoothLeService.CHARACTERISTIC)){

            GattAttributes.mDeviceName->{

                device_name_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mAppearance->{
                appearance_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mPeripheralParameters->{
                peripheral_pcp_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mDeviceId->{
                device_id_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mModelNumberString->{
                model_number_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mSerialNumberString->{
                serial_number_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mFirmwareRevision->{
                firmware_version_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mHardwareRevision->{
                hardware_version_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mSoftwareRevision->{
                software_version_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mManufacturerNameString->{
                manufacturer_name_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mCurrent->{
                current_content.text=(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)+" mA")
            }
            GattAttributes.mVoltage->{
                voltage_content.text=(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)+" V")
            }
            GattAttributes.mWatt->{
                watt_content.text=(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)+" W")
            }
            GattAttributes.mPowerFactor->{
                power_factor_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mLoad->{
                load_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mLoadDetected->{

                if (intent.getIntExtra(BluetoothLeService.EXTRA_DATA,0)==0){
                    load_detected_content.text=getString(R.string.non_loaded)
                }else{
                    load_detected_content.text=getString(R.string.loaded)
                }
            }
            GattAttributes.mGetTime->{
                get_time_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                set_time_content.text=getString(R.string.fail)
            }
            GattAttributes.mReadRecordedData -> {
                
                val array=intent.getStringArrayListExtra(BluetoothLeService.EXTRA_DATA)
                if (array != null) {
                    recorded_data_start_time_content.text=array.get(0)
                    recorded_data_end_time_content.text=array.get(1)
                    recorded_data_current_content.text=array.get(2)
                    recorded_data_voltage_content.text=array.get(3)
                    recorded_data_power_content.text=array.get(4)
                    recorded_data_power_factor_content.text=array.get(5)
                    recorded_data_consumption_content.text=array.get(6)
                    recorded_data_none_content.text=array.get(7)
                }
            }
            GattAttributes.mNfcTagId->{
                nfc_tag_id_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mChargingLatencyRead -> {

                val value=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
                val minutes=(value?.toInt()?:0)*5

                charging_latency_read_content.text=("$value     $minutes ${getString(R.string.minutes)}")

            }
            GattAttributes.mHardwareStatus->{
                hardware_status_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mSoftwareStatus->{
                software_status_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mErrorCode->{
                error_code_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mMachineStatus->{
                machine_status_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mMeterVersion->{
                meter_version_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
            }
            GattAttributes.mPlanChargingRead->{

            }
            GattAttributes.mAllChargingReport->{
                displayAllChargingData(intent)
            }
            GattAttributes.mPowerRead->{
                power_read_content.text=intent.getStringExtra(BluetoothLeService.EXTRA_DATA)
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
            R.id.power_read->{
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.activate_power))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
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

                switchOffNotifications()
                //power off command moves to switchOffNotification
                Log.d(TAG," power off")
            }
            R.id.set_time_title -> {

                val epochTime=Calendar.getInstance().timeInMillis/1000
                val timeIn16=epochTime.toString(16)

                Log.d(TAG,"real time is $epochTime")
                Log.d(TAG,"time is : $timeIn16")

                val hex1=timeIn16[0].toString()+timeIn16[1].toString()
                val hex2=timeIn16[2].toString()+timeIn16[3].toString()
                val hex3=timeIn16[4].toString()+timeIn16[5].toString()
                val hex4=timeIn16[6].toString()+timeIn16[7].toString()

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
            R.id.charging_latency_read_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.charging_latency))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.charging_latency_plus->{
                minutes++
                if (minutes>255){
                    minutes=0
                }
                charging_latency_show_number.text=((minutes*5).toString()+" ${getString(R.string.minutes)}")
            }
            R.id.charging_latency_minus->{
                minutes--

                if (minutes<0){
                    minutes=255
                }

                charging_latency_show_number.text=((minutes*5).toString()+" ${getString(R.string.minutes)}")
            }
            R.id.charging_latency_send_title->{

                if (charging_latency_input.text.isNullOrEmpty()){
                    minutes=0
                }else if(charging_latency_input.text.toString().toInt()>255){
                    Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
                    return
                }else{
                    minutes=charging_latency_input.text.toString().toInt()
                }

                val byteArray= byteArrayOf((minutes and 0xFF).toByte())

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
            R.id.machine_status_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.EXTRA_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.machine_status))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.meter_version_title->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.EXTRA_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.meter_parameter))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
            R.id.plan_charging_send->{
                checkAllPlanChargingDuration()
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.plan_charging))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,mPlanArray)
                }
            }
            R.id.charging_record_report->{
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.read_all_charging_record))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }
        }
    }

    private fun switchOnNotifications(){

        Thread{

            runOnUiThread {
                progressBar.visibility=View.VISIBLE
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.current))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            Thread.sleep(500)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.voltage))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            Thread.sleep(500)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.watt))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            Thread.sleep(500)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.power_factor))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            Thread.sleep(500)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.load))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
            }
            Thread.sleep(500)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.load_detected))
                if (characteristic!=null){
                    mBluetoothLeService?.setCharacteristicNotification(characteristic!!,true)
                }
                progressBar.visibility=View.GONE
            }

        }.start()
    }

    private fun switchOffNotifications(){

        Thread{

            runOnUiThread {
                progressBar.visibility=View.VISIBLE
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.current))
                if (characteristic!=null){
                    mBluetoothLeService?.disableCharacteristicNotification(characteristic!!)
                }
            }
            Thread.sleep(200)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.voltage))
                if (characteristic!=null){
                    mBluetoothLeService?.disableCharacteristicNotification(characteristic!!)
                }
            }
            Thread.sleep(200)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.watt))
                if (characteristic!=null){
                    mBluetoothLeService?.disableCharacteristicNotification(characteristic!!)
                }
            }
            Thread.sleep(200)
            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.POWER_MEASUREMENT)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.power_factor))
                if (characteristic!=null){
                    mBluetoothLeService?.disableCharacteristicNotification(characteristic!!)
                }
            }
            Thread.sleep(200)
            runOnUiThread {
                val byteArray= byteArrayOf(0x00)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.load))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            Thread.sleep(200)
            runOnUiThread {
                val byteArray= byteArrayOf(0x00)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.load_detected))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
            }
            Thread.sleep(200)
            runOnUiThread {
                val byteArray= byteArrayOf(0x00)
                characteristic=
                    (mBluetoothLeService?.getSupportedGattService(GattAttributes.DEVICE_CONTROL) as BluetoothGattService)
                        .getCharacteristic(UUID.fromString(GattAttributes.activate_power))
                if (characteristic!=null){
                    mBluetoothLeService?.writeCharacteristic(characteristic!!,byteArray)
                }
                progressBar.visibility=View.GONE
            }

        }.start()
    }
    private fun switchOnNewFunction(){
        Thread{

            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.EXTRA_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.machine_status))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }
            }

            Thread.sleep(200)

            runOnUiThread {
                characteristic=(mBluetoothLeService?.getSupportedGattService(GattAttributes.EXTRA_CONTROL)as BluetoothGattService)
                    .getCharacteristic(UUID.fromString(GattAttributes.meter_parameter))
                if (characteristic!=null){
                    mBluetoothLeService?.readCharacteristic(characteristic!!)
                }

            }

        }.start()
    }
    private fun checkAllPlanChargingDuration(){
        val tLatency0:Int
        val tLatency1:Int
        val tLatency2:Int
        val tLatency3:Int
        val tLatency4:Int

        val tCharging0:Int
        val tCharging1:Int
        val tCharging2:Int
        val tCharging3:Int
        val tCharging4:Int

        if (latency_0.text.isNullOrEmpty()){
            tLatency0=0
        }else if (latency_0.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tLatency0=latency_0.text.toString().toInt()
        }
        if (latency_1.text.isNullOrEmpty()){
            tLatency1=0
        }else if (latency_1.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tLatency1=latency_1.text.toString().toInt()
        }
        if (latency_2.text.isNullOrEmpty()){
            tLatency2=0
        }else if (latency_2.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tLatency2=latency_2.text.toString().toInt()
        }
        if (latency_3.text.isNullOrEmpty()){
            tLatency3=0
        }else if (latency_3.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tLatency3=latency_3.text.toString().toInt()
        }
        if (latency_4.text.isNullOrEmpty()){
            tLatency4=0
        }else if (latency_4.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tLatency4=latency_4.text.toString().toInt()
        }
        if (charging_0.text.isNullOrEmpty()){
            tCharging0=0
        }else if (charging_0.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tCharging0=charging_0.text.toString().toInt()
        }
        if (charging_1.text.isNullOrEmpty()){
            tCharging1=0
        }else if (charging_1.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tCharging1=charging_1.text.toString().toInt()
        }
        if (charging_2.text.isNullOrEmpty()){
            tCharging2=0
        }else if (charging_2.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tCharging2=charging_2.text.toString().toInt()
        }
        if (charging_3.text.isNullOrEmpty()){
            tCharging3=0
        }else if (charging_3.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tCharging3=charging_3.text.toString().toInt()
        }
        if (charging_4.text.isNullOrEmpty()){
            tCharging4=0
        }else if (charging_4.text.toString().toInt()>255){
            Toast.makeText(this,"數字請小於255",Toast.LENGTH_SHORT).show()
            return
        }else{
            tCharging4=charging_4.text.toString().toInt()
        }

        mPlanArray[0]= (tLatency0 and 0xFF).toByte()
        mPlanArray[1]=(tCharging0 and 0xFF).toByte()

        mPlanArray[2]= (tLatency1 and 0xFF).toByte()
        mPlanArray[3]=(tCharging1 and 0xFF).toByte()

        mPlanArray[4]= (tLatency2 and 0xFF).toByte()
        mPlanArray[5]=(tCharging2 and 0xFF).toByte()

        mPlanArray[6]= (tLatency3 and 0xFF).toByte()
        mPlanArray[7]=(tCharging3 and 0xFF).toByte()

        mPlanArray[8]= (tLatency4 and 0xFF).toByte()
        mPlanArray[9]=(tCharging4 and 0xFF).toByte()
    }
    private fun displayAllChargingData(intent: Intent){

        val llm=LinearLayoutManager(this)
        llm.orientation=LinearLayoutManager.VERTICAL
        record_recyclerview.layoutManager=llm
        reportList.clear()
        val adapter=ChargingDataAdapter(reportList)
        record_recyclerview.adapter=adapter

        val byteArray=intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA) as ByteArray

        if (byteArray.size<144){
            Toast.makeText(this,"sorry no data",Toast.LENGTH_SHORT).show()
            return
        }
        val titleArray:MutableMap<String,String> = mutableMapOf()
        titleArray[GattAttributes.mPlan]=""
        titleArray[GattAttributes.mStartTime]="Start\nTime"
        titleArray[GattAttributes.mEndTime]="End\nTime"
        titleArray[GattAttributes.mCurrent]="Current"
        titleArray[GattAttributes.mVoltage]="Voltage"
        titleArray[GattAttributes.mWatt]="Power"
        titleArray[GattAttributes.mPowerFactor]="Power\nFactor"
        titleArray[GattAttributes.mConsumption]="Consume"
        titleArray[GattAttributes.mNone]="None"

        reportList.add(titleArray)

        for (i in 0 until 6){

            val temp=byteArray.copyOfRange((i*24),(i*24+24))
            val reportArray:MutableMap<String,String> = mutableMapOf()

            val startSecond= (temp[0].toLong() and 0xFF)*256*65536+
                    (temp[1].toLong() and 0xFF)*256*256+
                    (temp[2].toLong() and 0xFF)*256+
                    (temp[3].toLong() and 0xFF)
            val startTimeData=getDate(startSecond)

            val endSecond=(temp[4].toLong() and 0xFF)*256*65536+
                    (temp[5].toLong() and 0xFF)*256*256+
                    (temp[6].toLong() and 0xFF)*256+
                    (temp[7].toLong() and 0xFF)
            val endTimeData=getDate(endSecond)

            val currentData=(((temp[8].toLong() and 0xFF)*256*256+
                    (temp[9].toLong() and 0xFF)*256+
                    (temp[10].toLong() and 0xFF)).toFloat()/1000f).toString()

            val voltageData=(((temp[11].toLong() and 0xFF)*256*256+
                    (temp[12].toLong() and 0xFF)*256+
                    (temp[13].toLong() and 0xFF)).toFloat()/1000f).toString()

            val powerData=(((temp[14].toLong() and 0xFF)*256*256+
                    (temp[15].toLong() and 0xFF)*256+
                    (temp[16].toLong() and 0xFF)).toFloat()/1000f).toString()

            val powerFactorData=(((temp[17].toLong() and 0xFF)*256+
                    (temp[18].toLong() and 0xFF)).toFloat()/10_000f).toString()

            val consumptionData=(((temp[19].toLong() and 0xFF)*256*256+
                    (temp[20].toLong() and 0xFF)*256+
                    (temp[21].toLong() and 0xFF)).toFloat()/1000f).toString()

            val noneData=((temp[22].toLong() and 0xFF)*256+
                    (temp[23].toLong() and 0xFF)).toString()

            reportArray[GattAttributes.mPlan]=("Plan ${i+1}")
            if (i==5){
                reportArray[GattAttributes.mPlan]=("AA21\nAA26")
            }
            reportArray[GattAttributes.mStartTime]=startTimeData
            reportArray[GattAttributes.mEndTime]=endTimeData
            reportArray[GattAttributes.mCurrent]=currentData
            reportArray[GattAttributes.mVoltage]=voltageData
            reportArray[GattAttributes.mWatt]=powerData
            reportArray[GattAttributes.mPowerFactor]=powerFactorData
            reportArray[GattAttributes.mConsumption]=consumptionData
            reportArray[GattAttributes.mNone]=noneData
            reportList.add(reportArray)
        }

        adapter.notifyDataSetChanged()
        scrollView.fullScroll(ScrollView.FOCUS_DOWN)
    }

    private fun getDate(seconds :Long):String{
        if (seconds==0L){
            return "no timing"
        }
        val formatter=SimpleDateFormat("yyyyMMdd\nHH:mm:ss", Locale.getDefault())

        val calendar=Calendar.getInstance()
        calendar.timeInMillis=seconds*1000
        return formatter.format(calendar.time)
    }

    private fun getFlatDate(seconds: Long):String{
        val format= SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault())

        val calendar=Calendar.getInstance()
        calendar.timeInMillis=seconds*1000
        return format.format(calendar.time)
    }
}