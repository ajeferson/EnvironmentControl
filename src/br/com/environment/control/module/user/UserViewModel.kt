package br.com.environment.control.module.user

import br.com.environment.control.extension.readIfExists
import br.com.environment.control.extension.takeIfExists
import br.com.environment.control.extension.write
import br.com.environment.control.model.Device
import br.com.environment.control.model.Environment
import br.com.environment.control.model.Message
import br.com.environment.control.model.User
import br.com.environment.control.module.ViewModel
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import net.jini.core.entry.Entry

class UserViewModel: ViewModel(), TableDataSource, TableDelegate {

    private val outColumns = arrayOf("Environment", "Users", "Devices")
    private val insideColumns = arrayOf("Users currently on the environment ")

    private lateinit var user: User
    private var users = listOf<User>()

    var title: PublishSubject<String> = PublishSubject.create()
    var status: BehaviorSubject<UserStatus> = BehaviorSubject.createDefault(UserStatus.OUTSIDE)
    val selectedDevice: PublishSubject<Int> = PublishSubject.create()

    var shouldPollMessages = true
    var shouldPollUsers = true

    override fun setup() {
        setupSpaces()
        createUser()
        fetchEnvironments(true)
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
            update.users = entry.users.map { it }
            update.users.add(user.id)
            update.devices = entry.devices.map { it }
            space.write(update)

            users = entry.users.map { User(it).also { it.environmentId = user.environmentId } }
                    .sortedBy { it.id }

        } catch(e: Exception) {
            error.onNext("Could not enter environment")
        }

        status.onNext(UserStatus.INSIDE)
        reload.onNext(Unit)
        title.onNext("${user.name} (${environments[index].name})")

        sendMessage("${user.name} entered ${environments[index].name}", true)
        pollMessages(true)
        pollUsers(true)

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
            val entry = space.takeIfExists(template)  as Environment
            val update = Environment(entry.id)
            update.users = entry.users.map { it }
            update.removeUser(user)
            update.devices = entry.devices.map { it }
            space.write(update)
        } catch(e: Exception) {
            error.onNext("Could not leave environment")
        }

        status.onNext(UserStatus.OUTSIDE)
        reload.onNext(Unit)
        title.onNext(user.name)

        pollMessages(false)
        pollUsers(false)
        sendMessage("${user.name} left env$envId", true, envId)

    }

    fun sendMessage(text: String, raw: Boolean = false, envId: Int? = null) {
        try {
            // Create base message
            val message = Message()
            if(raw) {
                message.content = text
            } else {
                message.setContent(user, text)
            }
            message.senderId = user.id

            // Send the message
            val templateEnv = Environment(envId ?: user.environmentId)
            val env = space.readIfExists(templateEnv) as Environment
            env.users.forEach {
                message.receiverId = it
                space.write(message)
            }

        } catch(e: Exception) {
            error.onNext("Could not send message")
        }
    }

    private fun pollMessages(shouldPoll: Boolean) {
        if(!shouldPoll) {
            shouldPollMessages = false
            return
        }
        shouldPollMessages = true
        val poll = Runnable {
            while(shouldPollMessages) {
                fetchMessages()
                Thread.sleep(POLL_SLEEP_TIME)
            }
        }
        Thread(poll).start()
    }

    private fun fetchMessages() {
        var entry: Entry?
        val template = Message()
        template.receiverId = user.id

        try {
            do {
                entry = space.takeIfExists(template)
                if(entry != null) {
                    val message = entry as Message
                    messages.onNext(message.content)
                }
            } while (entry != null)
        } catch(e: Exception) {
        }
    }

    private fun pollUsers(shouldPoll: Boolean) {
        if(!shouldPoll) {
            shouldPollUsers = false
            return
        }
        shouldPollUsers = true
        val poll = Runnable {
            while(shouldPollUsers) {
                fetchUsers()
                Thread.sleep(POLL_SLEEP_TIME)
            }
        }
        Thread(poll).start()
    }

    private fun fetchUsers() {
        try {
            val template = Environment(user.environmentId)
            val entry = space.readIfExists(template) as Environment
            users = entry.users.map { User(it).also { it.environmentId = user.environmentId } }
                    .sortedBy { it.id }
            reload.onNext(Unit)
        } catch(e: Exception) {
        }
    }

    fun createDevice() {
        try {
            fetchOrCreateMeta()
            val devId = meta.deviceId + 1

            // Write the device
            val device = Device(devId)
            device.environmentId = user.environmentId
            space.write(device)

            // Update environment
            val templateEnv = Environment(user.environmentId)
            val entry = space.takeIfExists(templateEnv) ?: return
            val env = entry as? Environment ?: return
            val update = Environment(env.id)
            update.users = env.users.map { it }
            update.devices = env.devices.map { it }
            update.addDevice(device)
            space.write(update)

            updateMeta(devId = devId)
            messages.onNext("${user.name} created ${device.name}")
        } catch (e: Exception) {
            error.onNext("Could not create device")
        }
    }

    fun refresh() {
        fetchEnvironments(true)
    }

    fun showDevices(index: Int) {
        if(index < 0) {
            return
        }
        if(environments[index].devices.size == 0) {
            error.onNext("There are no devices in this environment")
            return
        }
        fetchEnvironments(false)
        selectedDevice.onNext(environments[index].id)
    }

    /**
     * Table DataSource
     * */
    override fun numberOfRows(): Int {
        return if(status.value == UserStatus.OUTSIDE) {
            environments.size
        } else {
            users.size
        }
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
        return if(status.value == UserStatus.OUTSIDE) {
            val environment = environments[row]
            when(column) {
                0 -> environment.name
                1 -> environment.users.size
                else -> environment.devices.size
            }
        } else {
            users[row].name
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

    companion object {

        private const val POLL_SLEEP_TIME = 500L

    }

}