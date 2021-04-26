package com.example.testpdu

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ContentActivity : AppCompatActivity() {

    val TAG= ContentActivity::class.java.simpleName


    private lateinit var mDeviceName:String
    private lateinit var mDeviceAddress:String
    private var mGattServiceList:List<BluetoothGattService>?=null

    private var mBluetoothLeService: BluetoothLeService? = null
    private var isConnected=false

    private lateinit var tvDeviceName:TextView
    private lateinit var tvAppearance:TextView
    private lateinit var tvPeripheralParameters:TextView
    private lateinit var tvDeviceId:TextView
    private lateinit var tvModelNumber:TextView
    private lateinit var tvSerialNumber:TextView
    private lateinit var tvFirmwareRevision:TextView
    private lateinit var tvHardwareRevision:TextView
    private lateinit var tvSoftwareRevision:TextView
    private lateinit var tvCurrent:TextView
    private lateinit var tvVoltage:TextView
    private lateinit var tvWatt:TextView
    private lateinit var tvPowerFactor:TextView
    private lateinit var tvLoad:TextView
    private lateinit var tvLoadDetected:TextView
    private lateinit var tvActivatePower:TextView
    private lateinit var tvSetTime:TextView
    private lateinit var tvDownloadRequest:TextView
    private lateinit var tvReadRecordedData:TextView
    private lateinit var tvNfcTagId:TextView
    private lateinit var tvChargingLatency:TextView
    private lateinit var tvHardwareStatus:TextView
    private lateinit var tvSoftwareStatus:TextView
    private lateinit var tvErrorCode:TextView

    private lateinit var tvConnection:TextView

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
        tvCurrent=findViewById(R.id.current_content)
        tvVoltage=findViewById(R.id.voltage_content)
        tvWatt=findViewById(R.id.watt_content)
        tvPowerFactor=findViewById(R.id.power_factor_content)
        tvLoad=findViewById(R.id.load_content)
        tvLoadDetected=findViewById(R.id.load_detected_content)
        tvActivatePower=findViewById(R.id.activate_power_content)
        tvSetTime=findViewById(R.id.set_time_content)
        tvDownloadRequest=findViewById(R.id.download_request_content)
        tvReadRecordedData=findViewById(R.id.recorded_data_content)
        tvNfcTagId=findViewById(R.id.nfc_tag_id_content)
        tvChargingLatency=findViewById(R.id.charging_latency_content)
        tvHardwareStatus=findViewById(R.id.hardware_status_content)
        tvSoftwareStatus=findViewById(R.id.software_status_content)
        tvErrorCode=findViewById(R.id.error_code_content)

        tvConnection=findViewById(R.id.show_connect_status)


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

    private fun updateConnectionState(resourceID:Int){

        runOnUiThread {
            tvConnection.text = getString(resourceID)
        }

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

            val action=intent.action

            if (BluetoothLeService.ACTION_GATT_CONNECTED==action){
                isConnected=true
                updateConnectionState(R.id.menu_connect)
                invalidateOptionsMenu()
            }else if (BluetoothLeService.ACTION_GATT_DISCONNECTED==action){
                isConnected=false
                updateConnectionState(R.id.menu_disconnect)
                invalidateOptionsMenu()
            }else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED==action){

                if (mBluetoothLeService==null){
                    mGattServiceList=null
                }else {
                    mGattServiceList = mBluetoothLeService!!.getSupportedGattServices()
                    runGatt(mGattServiceList)
                }
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE==action){

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

    private fun runGatt(serviceList:List<BluetoothGattService>?){

        if (serviceList==null)return

        for (service in serviceList){

            for (characteristic in service.characteristics){
                Log.d(TAG,"charas is ${characteristic.uuid}")
            }
        }
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

            }
            GattAttributes.mDeviceId->{

            }
            GattAttributes.mModelNumberString->{

            }
            GattAttributes.mSerialNumberString->{

            }
            GattAttributes.mFirmwareRevision->{

            }
            GattAttributes.mHardwareRevision->{

            }
            GattAttributes.mSoftwareRevision->{

            }
            GattAttributes.mCurrent->{

            }
            GattAttributes.mVoltage->{

            }
            GattAttributes.mWatt->{

            }
            GattAttributes.mPowerFactor->{

            }
            GattAttributes.mLoad->{

            }
            GattAttributes.mLoadDetected->{

            }
            GattAttributes.mActivatePower->{

            }
            GattAttributes.mSetTime->{

            }
            GattAttributes.mDownloadRequest->{

            }
            GattAttributes.mReadRecordedData->{

            }
            GattAttributes.mNfcTagId->{

            }
            GattAttributes.mChargingLatency->{

            }
            GattAttributes.mHardwareStatus->{

            }
            GattAttributes.mSoftwareStatus->{

            }
            GattAttributes.mErrorCode->{

            }

        }
    }
}