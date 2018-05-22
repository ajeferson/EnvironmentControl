package br.com.environment.control.module.manager

import br.com.environment.control.extension.takeIfExists
import br.com.environment.control.extension.write
import br.com.environment.control.model.Environment
import br.com.environment.control.module.ViewModel
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.PublishSubject


class ManagerViewModel: ViewModel(), TableDataSource, TableDelegate {

    private val columns = arrayOf("Environment", "Users", "Devices")

    val selectedEnvUsers: PublishSubject<Int> = PublishSubject.create()
    val selectedEnvDevices: PublishSubject<Int> = PublishSubject.create()

    override fun setup() {
        setupSpaces()
        fetchEnvironments(true)
//        Cleaner(space).clean()
//        pollEnvironments()
    }

    fun createEnvironment() {
        try {
            fetchEnvironments(false)
            fetchOrCreateMeta()

            val id = meta.environmentId + 1
            val env = Environment(id)
            env.users = listOf()
            env.devices = listOf()
            space.write(env)

            updateMeta(id)

            environments.add(env)
            environments.sortBy { it.id }
            reload.onNext(Unit)

            messages.onNext("Created ${env.name}")
        } catch (e: Exception) {
        }
    }

    fun removeEnvironment(index: Int) {
        if(index < 0 || index >= environments.size) {
            return
        }
        try {
            fetchEnvironments(false)
            val env = environments[index]
            if (env.users.size > 0) {
                error.onNext("Can't delete because the environment has users in it")
                return
            }
            if (env.devices.size > 0) {
                error.onNext("Can't delete because the environment has devices in it")
                return
            }

            // Update list
            val template = Environment(env.id)
            space.takeIfExists(template)

            environments.removeAt(index)
            reload.onNext(Unit)

            messages.onNext("Removed ${env.name}")
        } catch (e: Exception) {
            error.onNext("Could not remove environment")
        }
    }

    fun showUsersOfEnvironment(index: Int) {
        fetchEnvironments(false)
        if(index < 0 || index >= environments.size) {
            return
        }
        if(environments[index].users.isEmpty()) {
            error.onNext("There are no users in this environment")
            return
        }
        selectedEnvUsers.onNext(environments[index].id)
    }

    fun showDevicesOfEnvironment(index: Int) {
        fetchEnvironments(false)
        if(index < 0 || index >= environments.size) {
            return
        }
        if(environments[index].devices.isEmpty()) {
            error.onNext("There are no devices in this environment")
            return
        }
        selectedEnvDevices.onNext(environments[index].id)
    }

    fun refresh() {
        fetchEnvironments(true)
    }

    /**
     * Table DataSource
     * */
    override fun numberOfRows(): Int {
        return environments.size
    }

    override fun numberOfColumns(): Int {
        return columns.size
    }

    override fun columnNameAt(index: Int): String {
        return columns[index]
    }

    override fun valueAt(row: Int, column: Int): Any {
        val environment = environments[row]
        return when(column) {
            0 -> environment.name
            1 -> environment.users.size
            else -> environment.devices.size
        }
    }

    override fun canEditCell(row: Int, column: Int): Boolean {
        return false
    }


    override fun didChangeTable(row: Int, column: Int, value: Any) {
    }

}