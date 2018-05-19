package br.com.environment.control.protocol

interface TableDelegate {

    fun didChangeTable(row: Int, column: Int, value: Any)

}