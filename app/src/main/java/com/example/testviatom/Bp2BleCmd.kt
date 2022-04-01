package com.example.testviatom

object Bp2BleCmd {


    val RT_PARAM=0x06
    val RT_WAVE=0x07
    val RT_DATA=0x08
    val SWTICH_STATE=0x09

    private var seqNo=0
    private fun addNo(){
        seqNo++
        if (seqNo>=255){
            seqNo=0
        }
    }

    fun getRtData(): ByteArray {
        val len = 0
        val cmd = ByteArray(8 + len)
        cmd[0] = 0xA5.toByte()
        cmd[1] = RT_DATA.toByte()
        cmd[2] = RT_DATA.inv().toByte()
        cmd[3] = 0x00.toByte()
        cmd[4] = seqNo.toByte()
        cmd[5] = len.toByte()
        cmd[6] = (len shr 8).toByte()
        cmd[7] = BleCRC.calCRC8(cmd)
        addNo()
        return cmd
    }
    fun switchState(state:Int):ByteArray{
        val len=1
        val cmd=ByteArray(8+len)
        cmd[0]=0xA5.toByte()
        cmd[1]= SWTICH_STATE.toByte()
        cmd[2]= SWTICH_STATE.inv().toByte()
        cmd[3]=0x00.toByte()
        cmd[4]= seqNo.toByte()
        cmd[5]=len.toByte()
        cmd[6]=0x00.toByte()
        cmd[7]=state.toByte()
        cmd[8]=BleCRC.calCRC8(cmd)
        addNo()
        return cmd
    }
}