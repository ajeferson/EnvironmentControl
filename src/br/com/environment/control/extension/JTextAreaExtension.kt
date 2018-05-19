package br.com.environment.control.extension

import javax.swing.JTextArea

fun JTextArea.coolAppend(text: String) {
    append("$text\n")
}