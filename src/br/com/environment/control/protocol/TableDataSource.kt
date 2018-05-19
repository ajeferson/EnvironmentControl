package br.com.environment.control.protocol

interface TableDataSource {

    fun numberOfRows(): Int
    fun numberOfColumns(): Int
    fun columnNameAt(index: Int): String
    fun valueAt(row: Int, column: Int): Any
    fun canEditCell(row: Int, column: Int): Boolean

}