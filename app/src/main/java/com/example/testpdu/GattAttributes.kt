package com.example.testpdu

class GattAttributes {

    companion object{

        val CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

        val GENERAL_ACCESS = "00001800-0000-1000-8000-00805f9b34fb"
        val GENERAL_ATTRIBUTES = "00001801-0000-1000-8000-00805f9b34fb"
        val DEVICE_INFORMATION = "0000180A-0000-1000-8000-00805f9b34fb"
        val POWER_MEASUREMENT = "0000AA10-0000-1000-8000-00805f9b34fb"
        val DEVICE_CONTROL = "0000AA20-0000-1000-8000-00805f9b34fb"
        val SYSTEM_STATUS = "0000AA30-0000-1000-8000-00805f9b34fb"


        val device_name = "00002A00-0000-1000-8000-00805f9b34fb"
        val appearance = "00002A01-0000-1000-8000-00805f9b34fb"
        val peripheral_preferred_connection_parameters = "00002A04-0000-1000-8000-00805f9b34fb"

        val device_id = "00002A23-0000-1000-8000-00805f9b34fb"
        val model_number_string = "00002A24-0000-1000-8000-00805f9b34fb"
        val serial_number_string = "00002A25-0000-1000-8000-00805f9b34fb"
        val firmware_revision_string = "00002A26-0000-1000-8000-00805f9b34fb"
        val hardware_revision_string = "00002A27-0000-1000-8000-00805f9b34fb"
        val sofrware_revision_string = "00002A28-0000-1000-8000-00805f9b34fb"

        val current = "0000AA11-0000-1000-8000-00805f9b34fb"
        val voltage = "0000AA12-0000-1000-8000-00805f9b34fb"
        val watt = "0000AA13-0000-1000-8000-00805f9b34fb"
        val power_factor = "0000AA14-0000-1000-8000-00805f9b34fb"
        val load = "0000AA15-0000-1000-8000-00805f9b34fb"
        val load_detected = "0000AA16-0000-1000-8000-00805f9b34fb"

        val activate_power = "0000AA21-0000-1000-8000-00805f9b34fb"
        val set_time = "0000AA22-0000-1000-8000-00805f9b34fb"
        val download_request = "0000AA23-0000-1000-8000-00805f9b34fb"
        val read_recorded_data = "0000AA24-0000-1000-8000-00805f9b34fb"
        val nfc_tag_id = "0000AA25-0000-1000-8000-00805f9b34fb"
        val charging_latency = "0000AA26-0000-1000-8000-00805f9b34fb"

        val hardware_status = "0000AA31-0000-1000-8000-00805f9b34fb"
        val software_status = "0000AA32-0000-1000-8000-00805f9b34fb"
        val error_code = "0000AA33-0000-1000-8000-00805f9b34fb"

    }



}