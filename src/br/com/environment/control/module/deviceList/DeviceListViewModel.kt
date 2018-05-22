package br.com.environment.control.module.deviceList

import br.com.environment.control.extension.readIfExists
import br.com.environment.control.model.Device
import br.com.environment.control.model.Environment
import br.com.environment.control.model.User
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.PublishSubject
import net.jini.space.JavaSpace

class DeviceListViewModel(private val space: JavaSpace, private val envId: Int): TableDataSource, TableDelegate {

    private var devices = listOf<Device>()
    private lateinit var environment: Environment

    val reload: PublishSubject<Unit> = PublishSubject.create()

    fun setup() {
        fetchEnvironment()
        fetchDevices()
        reload.onNext(Unit)
    }

    private fun fetchEnvironment() {
        val template = Environment(envId)
        val entry = space.readIfExists(template) ?: return
        environment = entry as? Environment ?: return
    }

    private fun fetchDevices() {
        devices = environment.devices
                .map { Device(it) }
                .sortedBy { it.id }
    }

    /**
     * Table DataSource
     * */

    override fun numberOfRows(): Int {
        return devices.size
    }

    override fun numberOfColumns(): Int {
        return 1
    }

    override fun columnNameAt(index: Int): String {
        return "Devices inside env$envId"
    }

    override fun valueAt(row: Int, column: Int): Any {
        return devices[row].name
    }

    override fun canEditCell(row: Int, column: Int): Boolean {
        return false
    }


    /**
     * Table Delegate
     * */

    override fun didChangeTable(row: Int, column: Int, value: Any) {
    }

}