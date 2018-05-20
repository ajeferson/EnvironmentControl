package br.com.environment.control.module.user

import br.com.environment.control.common.Lookup
import br.com.environment.control.extension.readIfExists
import br.com.environment.control.extension.takeIfExists
import br.com.environment.control.extension.write
import br.com.environment.control.model.Environment
import br.com.environment.control.model.Meta
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.PublishSubject
import net.jini.core.entry.Entry
import net.jini.space.JavaSpace

class UserViewModel(private val name: String): TableDataSource, TableDelegate {

    private val columns = arrayOf("Environment", "Users", "Devices")

    private var environments = mutableListOf<Environment>()

    private lateinit var space: JavaSpace
    private lateinit var meta: Meta

    val error: PublishSubject<String> = PublishSubject.create()
    val messages: PublishSubject<String> = PublishSubject.create()
    val reload: PublishSubject<Unit> = PublishSubject.create()

    var poll = Runnable {
        while (true) {
            Thread.sleep(POLL_SLEEP_TIME)
            fetchEnvironments(true)
        }
    }

    fun setup() {
        setupSpaces()
        fetchEnvironments(true)
//        Cleaner(space).clean()
        Thread(poll).start()
    }

    private fun setupSpaces() {
        try {
            messages.onNext("Connecting...")
            val finder = Lookup(JavaSpace::class.java)
            space = finder.service as JavaSpace
            messages.onNext("Connected")
        } catch (e: Exception) {
            error.onNext("Could not connect to spaces")
            e.printStackTrace()
        }
    }

    private fun fetchOrCreateMeta() {
        try {
            val template = Meta()
            val entry = space.readIfExists(template)
            if(entry == null) {
                meta = Meta.defaultMeta()
                space.write(meta)
            } else {
                meta = entry as Meta
            }
        } catch (e: Exception) {
        }
    }

    private fun updateMeta(envId: Int) {
        try {
            val update = Meta()
            space.takeIfExists(update)
            update.environmentId = envId
            space.write(update)
        } catch (e: Exception) {
        }
    }

    fun fetchEnvironments(log: Boolean) {
        try {
            if(log) {
                messages.onNext("Fetching list of environments...")
            }

            fetchOrCreateMeta()
            environments.clear()

            val max = meta.environmentId
            var id = 1
            val template = Environment()
            var entry: Entry?
            while(id <= max) {
                template.id = id
                entry = space.readIfExists(template)
                if(entry != null) {
                    environments.add(entry as Environment)
                }
                id += 1
            }

            environments.sortBy { it.id }
            reload.onNext(Unit)
        } catch (e: Exception) {
            if(log) {
                error.onNext("Error while fetching the list of environments")
            }
        }
    }

    fun createEnvironment() {
        try {
            fetchEnvironments(false)
            fetchOrCreateMeta()

            val id = meta.environmentId + 1
            val env = Environment(id)
            space.write(env)

            updateMeta(id)

            environments.add(env)
            environments.sortBy { it.id }
            reload.onNext(Unit)
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
            if (env.users > 0) {
                error.onNext("Can't delete because the environment has users in it")
                return
            }
            if (env.devices > 0) {
                error.onNext("Can't delete because the environment has devices in it")
                return
            }

            // Update list
            val template = Environment(env.id)
            space.takeIfExists(template)

            environments.removeAt(index)
            reload.onNext(Unit)
        } catch (e: Exception) {
            error.onNext("Could not remove environment")
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

    companion object {

        private const val POLL_SLEEP_TIME = 3_000L

    }

}