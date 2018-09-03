package io.sellmair.quantum.internal

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal class SynchronizedHistory<T> : MutableHistory<T> {
    /**
     * Lock used to synchronize access to the internal list of states [states]
     */
    private val lock = ReentrantLock()

    /**
     * ALl states that are currently stored in this history.
     */
    private val states = LinkedList<T>()


    override var enabled: Boolean = false
        set(value) = lock.withLock {
            field = value
            if (!value) {
                states.clear()
            }
        }


    /**
     * Adds a new state to the history.
     * The state will only be added if the history is [enabled].
     * Old states will be removed if the limit is reached.
     */
    override fun add(state: T) = lock.withLock {
        if (enabled) {
            states.add(state)
            val limit = this.limit
            if (limit != null) {
                while (states.size > limit) {
                    states.removeFirst()
                }
            }
        }
    }

    override fun clear() = lock.withLock {
        states.clear()
    }


    override var limit: Int? = null
        set(value) {
            /*
            Don't allow negative limits.
             */
            if (value != null && value < 0) {
                throw IllegalStateException("Expected limit to be > 0. Was: $value")
            }

            field = value
        }

    /**
     * Will create a snapshot of the current history and return the snapshots
     * iterator.
     */
    override fun iterator(): Iterator<T> = lock.withLock {
        val list = ArrayList<T>(states.size)
        list.addAll(states)
        list.iterator()
    }

}