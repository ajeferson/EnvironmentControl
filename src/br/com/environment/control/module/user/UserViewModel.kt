package br.com.environment.control.module.user

import br.com.environment.control.extension.write
import br.com.environment.control.model.User
import br.com.environment.control.module.ViewModel
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class UserViewModel: ViewModel(), TableDataSource, TableDelegate {

    private val columns = arrayOf("Environment", "Users", "Devices")

    private lateinit var user: User

    var title: PublishSubject<String> = PublishSubject.create()
    var status: BehaviorSubject<UserStatus> = BehaviorSubject.createDefault(UserStatus.OUTSIDE)

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
            user = User(meta.userId + 1)
            space.write(user)
            title.onNext(user.name)
            updateMeta(userId = user.id)
        } catch(e: Exception) {
            error.onNext("Could not subscribe new user. Exiting.")
            System.exit(1)
        }
    }

    private fun updateEnvironment(envId: Int?) {
        try {
            val update = User(user.id)
            update.environmentId = envId
            space.write(update)
            user = update
            reload.onNext(Unit)
        } catch(e: Exception) {
            val action = if(envId != null) "enter" else "leave"
            error.onNext("Could not $action environment")
        }
    }

    fun enterEnvironment(index: Int) {
        if(index < 0) {
            return
        }

        val env = environments[index]
        updateEnvironment(env.id)

        status.onNext(UserStatus.INSIDE)
        title.onNext("${user.name} (${env.name})")
    }

    fun leaveEnvironment() {
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