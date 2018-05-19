package br.com.environment.control.common

import br.com.environment.control.model.Environment
import br.com.environment.control.model.EnvironmentList
import net.jini.core.entry.Entry
import net.jini.space.JavaSpace

class Cleaner(private val space: JavaSpace) {

    fun clean() {
        var entry: Entry? = null
        val entries: List<Entry> = listOf(Environment(), EnvironmentList())
        var index = 0
        do {
            entry = space.takeIfExists(entries[index], null, 1000)
            if(entry != null) {
                println("Took $entry")
            } else {
                index += 1
            }
        } while (index < entries.size)
    }

}