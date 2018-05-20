package br.com.environment.control.extension

import net.jini.core.entry.Entry
import net.jini.core.lease.Lease
import net.jini.space.JavaSpace

fun JavaSpace.readIfExists(template: Entry): Entry? {
    return readIfExists(template, null, TIMEOUT)
}

fun JavaSpace.takeIfExists(template: Entry): Entry? {
    return takeIfExists(template, null, TIMEOUT)
}

fun JavaSpace.write(template: Entry) {
    write(template, null, Lease.FOREVER)
}

const val TIMEOUT = 10_000L