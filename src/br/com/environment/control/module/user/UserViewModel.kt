package br.com.environment.control.module.user

import br.com.environment.control.extension.write
import br.com.environment.control.model.User
import br.com.environment.control.module.ViewModel
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate

class UserViewModel: ViewModel(), TableDataSource, TableDelegate {

    private val columns = arrayOf("Environment", "Users", "Devices")

    private lateinit var user: User

    override fun setup() {
        setupSpaces()
        createUser()
        fetchEnvironments(true)
        pollEnvironments()
    }

    private fun createUser() {
        try {
            messages.onNext("Subscribing new user...")
            fetchOrCreateMeta()
            var id = meta.userId + 1
            user = User(id)
            space.write(user)
        } catch(e: Exception) {
            error.onNext("Could not subscribe new user. Exiting.")
            System.exit(1)
        }
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
            1 -> environment.users
            else -> environment.devices
        }
    }

    override fun canEditCell(row: Int, column: Int): Boolean {
        return false
    }


    override fun didChangeTable(row: Int, column: Int, value: Any) {
    }

}