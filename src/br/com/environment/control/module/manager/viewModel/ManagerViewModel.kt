package br.com.environment.control.module.manager.viewModel

import br.com.environment.control.common.Cleaner
import br.com.environment.control.common.Lookup
import br.com.environment.control.model.Environment
import br.com.environment.control.model.TupleEnvironments
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

//    private val nextId: Int
//        get() {
//            if(environments.isEmpty()) {
//                return 1
//            }
//            return environments.sortedBy { it.id }.last().id + 1
//        }

    fun setup() {
        setupSpaces()
    }

    private fun setupSpaces() {
        try {
            messages.onNext("Connecting...")
            val finder = Lookup(JavaSpace::class.java)
            space = finder.service as JavaSpace
            messages.onNext("Connected")
//            Cleaner(space).clean()
        } catch (e: Exception) {
            error.onNext("Could not connect to spaces")
            e.printStackTrace()
        }
    }

    fun fetchOrCreateList() {
        try {
            messages.onNext("Fetching list of environments...")
            val template = TupleEnvironments()
            val entry = space.readIfExists(template, null, READ_TIMEOUT)

            if(entry == null) {
                template.initEnvironments()
                space.write(template, null, Lease.FOREVER)
            } else {
                val envs = (entry as TupleEnvironments)
                        .environments
                        .sortedBy { it }
                environments = envs.map { Environment(it) }
                        .toMutableList()
                reload.onNext(Unit)
            }
        } catch (e: Exception) {
            error.onNext("Error while fetching the list of environments")
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
            space.write(environment, null, Lease.FOREVER)

            // Get the last list
            val template = TupleEnvironments()
            val tuple = space.take(template, null, READ_TIMEOUT) as TupleEnvironments

            // Update the list
            val updated = TupleEnvironments()
            updated.environments = tuple.environments
            updated.addEnvironment(environment)
            space.write(updated, null, Lease.FOREVER)

            reload.onNext(Unit)
        } catch (e: Exception) {
            error.onNext("Error creating environment")
            e.printStackTrace()
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
        space.write(environment, null, (60 * 1000).toLong())
        return environment.name
    }


    /**
     * Table Delegate
     * */

    override fun didChangeName(index: Int, name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didChangePhoneNumber(index: Int, phoneNumber: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {

        private const val READ_TIMEOUT = 10_000L

    }

}