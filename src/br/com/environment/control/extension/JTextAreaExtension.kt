package br.com.environment.control.extension

import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JTextArea

val formatter = SimpleDateFormat("HH:mm:ss")

fun JTextArea.coolAppend(text: String) {
    val strDate = formatter.format(Date())
    append("[$strDate] $text\n")
    caretPosition = document.length
}