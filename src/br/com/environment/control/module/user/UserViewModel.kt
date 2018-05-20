package br.com.environment.control.module.user

import br.com.environment.control.extension.readIfExists
import br.com.environment.control.extension.takeIfExists
import br.com.environment.control.extension.write
import br.com.environment.control.model.Environment
import br.com.environment.control.model.User
import br.com.environment.control.module.ViewModel
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class UserViewModel: ViewModel(), TableDataSource, TableDelegate {

    private val outColumns = arrayOf("Environment", "Users", "Devices")
    private val insideColumns = arrayOf("Users currently on the environment ")

    private lateinit var user: User
    private var users = listOf<User>()

    var title: PublishSubject<String> = PublishSubject.create()
    var status: BehaviorSubject<UserStatus> = BehaviorSubject.createDefault(UserStatus.OUTSIDE)

    override fun setup() {
        setupSpaces()
        createUser()
        fetchEnvironments(true)
//        pollEnvironments()
    }

    private fun createUser() {
        try {
            messages.onNext("Subscribing new user...")
            fetchOrCreateMeta()
            user = User(meta.userId + 1)
            space.write(user)
            title.onNext(user.name)
            updateMeta(userId = user.id)
        } catch(e: Exception) {
            error.onNext("Could not subscribe new user. Exiting.")
            System.exit(1)
        }
    }

    fun enterEnvironment(index: Int) {
        if(index < 0) {
            return
        }

        val envId = environments[index].id
        try {
            // Update user tuple
            space.takeIfExists(User(user.id)) // Remove from space
            val updateUser = User(user.id)
            updateUser.environmentId = envId
            space.write(updateUser) // Write back
            user = updateUser

            // Update environment tuple
            val template = Environment(envId)
            val entry = space.takeIfExists(template) as Environment
            val update = Environment(entry.id)
            update.users = entry.users + 1
            update.devices = entry.devices
            space.write(update)
        } catch(e: Exception) {
            error.onNext("Could not enter environment")
        }

        status.onNext(UserStatus.INSIDE)
        reload.onNext(Unit)
        title.onNext("${user.name} (${environments[index].name})")
    }

    fun leaveEnvironment() {
        val envId = user.environmentId
        try {
            // Update user tuple
            space.takeIfExists(User(user.id)) // Remove from space
            val updateUser = User(user.id)
            updateUser.environmentId = null
            space.write(updateUser) // Write back
            user = updateUser

            // Update environment tuple
            val template = Environment(envId)
            val entry = space.takeIfExists(template) as Environment
            val update = Environment(entry.id)
            update.users = entry.users - 1
            update.devices = entry.devices
            space.write(update)
        } catch(e: Exception) {
            error.onNext("Could not leave environment")
        }

        status.onNext(UserStatus.OUTSIDE)
        reload.onNext(Unit)
        title.onNext(user.name)
    }

    /**
     * Table DataSource
     * */
    override fun numberOfRows(): Int {
        return environments.size
    }

    override fun numberOfColumns(): Int {
        return when(status.value) {
            UserStatus.OUTSIDE -> outColumns.size
            else -> insideColumns.size
        }
    }

    override fun columnNameAt(index: Int): String {
        return when(status.value) {
            UserStatus.OUTSIDE -> outColumns[index]
            else -> insideColumns[index]
        }
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


    /**
     * Status
     * */

    enum class UserStatus {
        OUTSIDE,
        INSIDE;

        val isInside: Boolean get() = this == INSIDE
        val isOutside: Boolean get() = this == OUTSIDE
    }

}