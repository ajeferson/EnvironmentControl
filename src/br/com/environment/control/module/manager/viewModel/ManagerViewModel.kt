package br.com.environment.control.module.manager.viewModel

import br.com.environment.control.common.Lookup
import br.com.environment.control.model.Environment
import br.com.environment.control.model.EnvironmentList
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.PublishSubject
import net.jini.core.lease.Lease
import net.jini.space.JavaSpace


class ManagerViewModel: TableDataSource, TableDelegate {

    private val columns = arrayOf("Environment", "Users", "Devices")

    private var environments = mutableListOf<Environment>()

    private lateinit var space: JavaSpace

    val error: PublishSubject<String> = PublishSubject.create()
    val messages: PublishSubject<String> = PublishSubject.create()
    val reload: PublishSubject<Unit> = PublishSubject.create()

    var poll = Runnable {
        while (true) {
            Thread.sleep(POLL_SLEEP_TIME)
            fetchOrCreateList(false)
        }
    }

    fun setup() {
        setupSpaces()
        Thread(poll).start()
    }

    private fun setupSpaces() {
        try {
            messages.onNext("Connecting...")
            val finder = Lookup(JavaSpace::class.java)
            space = finder.service as JavaSpace
            messages.onNext("Connected")
//            Cleaner(space).clean()
            fetchOrCreateList(true)
        } catch (e: Exception) {
            error.onNext("Could not connect to spaces")
            e.printStackTrace()
        }
    }

    private fun fetchOrCreateList(log: Boolean) {
        try {
            if(log) {
                messages.onNext("Fetching list of environments...")
            }
            val template = EnvironmentList()
            val entry = space.readIfExists(template, null, READ_TIMEOUT)

            if(entry == null) {
                template.initEnvironments()
                space.write(template, null, Lease.FOREVER)
            } else {
                val envs = (entry as EnvironmentList)
                        .environments
                        .sortedBy { it.name }
                environments = envs.map { Environment(it.name) }
                        .toMutableList()
                reload.onNext(Unit)
            }
        } catch (e: Exception) {
            if(log) {
                error.onNext("Error while fetching the list of environments")
            }
        }
    }

    fun createEnvironment(name: String?) {
        if(name == null) {
            return
        }
        if(name.isEmpty()) {
            error.onNext("Name can't be blank")
            return
        }
        try {
            // Create the environment
            val environment = Environment()
            environment.name = name
            environments.add(environment)
            environments.sortBy { it.name }
//            space.write(environment, null, Lease.FOREVER)

            // Get the last list
            val template = EnvironmentList()
            val tuple = space.take(template, null, READ_TIMEOUT) as EnvironmentList

            // Update the list
            val updated = EnvironmentList()
            updated.environments = tuple.environments
                    .map { Environment(it.name) }
            updated.addEnvironment(environment)
            space.write(updated, null, Lease.FOREVER)

            reload.onNext(Unit)
        } catch (e: Exception) {
            error.onNext("Error creating environment")
            e.printStackTrace()
        }
    }

    fun removeEnvironment(index: Int) {
        if(index < 0 || index >= environments.size) {
            return
        }
        try {
            val template = EnvironmentList()
            val entryList = space.readIfExists(template, null, READ_TIMEOUT) as EnvironmentList

            val env = entryList.environments.first { it.name == environments[index].name }
            if (env.users > 0) {
                error.onNext("Can't delete because the environment has users in it")
                return
            }
            if (env.devices > 0) {
                error.onNext("Can't delete because the environment has devices in it")
                return
            }

            // Update list
            val list = space.take(template, null, READ_TIMEOUT) as EnvironmentList
            val updated = EnvironmentList()
            updated.environments = list.environments.map { it }.toMutableList()
            updated.removeEnvironment(environments[index])
            space.write(updated, null, Lease.FOREVER)

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

        private const val READ_TIMEOUT = 10_000L
        private const val POLL_SLEEP_TIME = 3_000L

    }

}