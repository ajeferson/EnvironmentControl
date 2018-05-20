package br.com.environment.control.module

import br.com.environment.control.common.Lookup
import br.com.environment.control.extension.readIfExists
import br.com.environment.control.extension.takeIfExists
import br.com.environment.control.extension.write
import br.com.environment.control.model.Environment
import br.com.environment.control.model.Meta
import io.reactivex.subjects.PublishSubject
import net.jini.core.entry.Entry
import net.jini.space.JavaSpace

abstract class ViewModel {

    protected lateinit var meta: Meta

    protected lateinit var space: JavaSpace

    val error: PublishSubject<String> = PublishSubject.create()
    val messages: PublishSubject<String> = PublishSubject.create()
    val reload: PublishSubject<Unit> = PublishSubject.create()

    protected var environments = mutableListOf<Environment>()

    private var poll = Runnable {
        while (true) {
            Thread.sleep(POLL_SLEEP_TIME)
            fetchEnvironments(false)
        }
    }

    abstract fun setup()

    protected fun pollEnvironments() {
        Thread(poll).start()
    }

    protected fun setupSpaces() {
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


    /**
     * Meta
     * */

    protected fun fetchOrCreateMeta() {
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

    protected fun updateMeta(envId: Int? = null, userId: Int? = null) {
        try {
            val template = Meta()
            val entry = space.takeIfExists(template) as Meta
            val update = Meta()
            update.environmentId = entry.environmentId
            update.userId = entry.userId
            if(envId != null) {
                update.environmentId = envId
            }
            if(userId != null) {
                update.userId = userId
            }
            space.write(update)
        } catch (e: Exception) {
        }
    }


    /**
     * Environments
     * */

    protected fun fetchEnvironments(log: Boolean) {
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


    /**
     * Companion
     * */

    companion object {

        private const val POLL_SLEEP_TIME = 3_000L

    }

}