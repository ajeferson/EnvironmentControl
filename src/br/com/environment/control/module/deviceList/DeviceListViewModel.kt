package br.com.environment.control.module.deviceList

import br.com.environment.control.extension.readIfExists
import br.com.environment.control.extension.takeIfExists
import br.com.environment.control.extension.write
import br.com.environment.control.model.Device
import br.com.environment.control.model.Environment
import br.com.environment.control.module.ViewModel
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.PublishSubject
import net.jini.space.JavaSpace

class DeviceListViewModel(space: JavaSpace, private val envId: Int): ViewModel(), TableDataSource, TableDelegate {

    private var devices = listOf<Device>()
    private lateinit var environment: Environment

    val envs: PublishSubject<Array<Int>> = PublishSubject.create()

    init {
        this.space = space
    }

    override fun setup() {
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

    fun touchedMoveDevice(index: Int) {
        if(index < 0) {
            return
        }
        try {
            fetchEnvironments(false)
            val list = environments
                    .filter { it.id != envId }
                    .map { it.id }
                    .toTypedArray()
            envs.onNext(list)
        } catch(e: Exception) {
        }
    }

    fun moveDevice(index: Int, id: Int) {
        if(index < 0) {
            return
        }
        try {
            val devId = devices[index].id

            // Remove device from environment
            var template = Environment(envId)
            var env = space.takeIfExists(template) as? Environment ?: return
            var update = Environment(envId)
            update.users = env.users.map { it }
            update.devices = env.devices.map { it }
            update.removeDevice(Device(devId))
            space.write(update)

            // Insert into new environment
            template = Environment(id)
            env = space.takeIfExists(template) as? Environment ?: return
            update = Environment(id)
            update.users = env.users.map { it }
            update.devices = env.devices.map { it }
            update.addDevice(Device((devId)))
            space.write(update)

            setup()
        } catch(e: Exception) {
            error.onNext("Could not move device")
        }
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