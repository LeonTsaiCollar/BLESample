package com.leontsai.blesample

data class GattAttributes(
    val DEVICE_NAME: String = ""//外设名称
    , val SERVICE_OPEN_DOOR: String = ""//Service的UUID
    , val CHARACTERISTIC_SEND_COMMAND: String = ""//发送命令特征值的UUID
    , val CHARACTERISTIC_RETURN_COMMAND: String = ""//返回命令特征值的UUID
)

