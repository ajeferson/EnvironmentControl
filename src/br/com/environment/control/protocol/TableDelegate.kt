package br.com.environment.control.protocol

interface TableDelegate {

    fun didChangeName(index: Int, name: String)
    fun didChangePhoneNumber(index: Int, phoneNumber: String)

}