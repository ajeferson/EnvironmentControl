package br.com.environment.control.module.userList

import br.com.environment.control.extension.readIfExists
import br.com.environment.control.model.Environment
import br.com.environment.control.model.User
import br.com.environment.control.protocol.TableDataSource
import br.com.environment.control.protocol.TableDelegate
import io.reactivex.subjects.PublishSubject
import net.jini.space.JavaSpace

class UserListViewModel(private val space: JavaSpace, private val envId: Int): TableDataSource, TableDelegate {

    private var users = listOf<User>()
    private var environment = Environment()

    var reload: PublishSubject<Unit> = PublishSubject.create()

    fun setup() {
        fetchEnvironment()
        fetchUsers()
        reload.onNext(Unit)
    }

    private fun fetchEnvironment() {
        val template = Environment(envId)
        environment = space.readIfExists(template) as Environment
    }

    private fun fetchUsers() {
        users = environment.users
                .map {User(it) }
                .sortedBy { it.id }
    }


    /**
     * Table DataSource
     * */

    override fun numberOfRows(): Int {
        return users.size
    }

    override fun numberOfColumns(): Int {
        return 1
    }

    override fun columnNameAt(index: Int): String {
        return "Users currently inside ${environment.name}"
    }

    override fun valueAt(row: Int, column: Int): Any {
        return users[row].name
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