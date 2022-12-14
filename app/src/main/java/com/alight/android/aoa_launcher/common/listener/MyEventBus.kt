package com.alight.android.aoa_launcher.common.listener


import com.alight.android.aoa_launcher.common.i.EventEmitterInterface
import java.util.*
import kotlin.collections.HashMap

class MyEventBus : EventEmitterInterface {


	// Default maxnumber of listeners
	override var defaultMaxListeners: Int = 10

	// HashMap store events
	private val events: HashMap<String, LinkedList<Listener>> = HashMap()

	/**
	 * initializer blocks
	 *
	 */
	init {
		//TODO
	}

	/**
	 * Synchronously calls each of the listeners registered for the event named eventName,
	 * in the order they were registered, passing the supplied arguments to each.
	 *
	 * @param eventName The name of the event
	 * @param args  Vararg arguments
	 * @return true if the event had listeners, false otherwise
	 */
	@Throws(Exception::class)
	override fun emit(eventName: String, vararg args: Any): Boolean {
		if (events.containsKey(eventName)) {
			var removeListeners: LinkedList<Listener> = LinkedList()
			for (listener in events.get(eventName)!!) {
				listener.listener.invoke(args)
				if (listener.isOnce) {
					removeListeners.add(listener)
				}
			}

			for (eventListener in removeListeners) {
				events.get(eventName)!!.remove(eventListener)
			}
			return true
		} else {
			return false
		}
	}

	/**
	 * Adds a one-time listener function for the event named eventName. The next time eventName is triggered, this listener is removed and then invoked.
	 * Multiple calls passing the same combination of eventName and listener will result in
	 * the listener being added, and called, multiple times.
	 *
	 * @param eventName The name of the event
	 * @param listener The callback function
	 * @return a reference to the EventEmitter, so that calls can be chained
	 */
	@Throws(Exception::class)
	override fun on(
		eventName: String,
		listener: (args: Array<out Any>) -> Unit
	): EventEmitterInterface {
		return addListener(eventName, listener)
	}

	/**
	 * Adds a one-time listener function for the event named eventName.
	 * The next time eventName is triggered, this listener is removed and then invoked.
	 *
	 * @param eventName The name of the event
	 * @param listener The callback function
	 * @return a reference to the EventEmitter, so that calls can be chained
	 */
	@Throws(Exception::class)
	override fun once(
		eventName: String,
		listener: (args: Array<out Any>) -> Unit
	): EventEmitterInterface {
		return addListener(eventName, listener, true)
	}

	/**
	 * Alias for emitter.on(eventName, listener).
	 *
	 * @param eventName The name of the event
	 * @param listener The callback function
	 * @param isOnce listener status loop(default) or once
	 * @return a reference to the EventEmitter, so that calls can be chained
	 */
	override fun addListener(
		eventName: String,
		listener: (args: Array<out Any>) -> Unit,
		isOnce: Boolean
	): EventEmitterInterface {
		if (defaultMaxListeners != 0 && listenerCount(eventName) >= defaultMaxListeners) {
			//maxListeners exception
		} else {
			if (!events.containsKey(eventName)) {
				events[eventName] = LinkedList()
			}
			events[eventName]?.add(Listener(listener, isOnce))
		}
		return this
	}

	/**
	 * Removes the specified listener from the listener array for the event named eventName.
	 *
	 * @param eventName The name of the event
	 * @param listener The callback function
	 * @return a reference to the EventEmitter, so that calls can be chained
	 */
	override fun removeListener(
		eventName: String,
		listener: (args: Array<out Any>) -> Unit
	): EventEmitterInterface {
		if (events.containsKey(eventName)) {
			var removeListeners: LinkedList<Listener> = LinkedList()
			for (eventListener in events.get(eventName)!!) {
				if (eventListener.listener === listener) {
					removeListeners.add(eventListener)
				}
			}
			for (eventListener in removeListeners) {
				events.get(eventName)!!.remove(eventListener)
			}
		}
		return this
	}

	/**
	 * Removes all listeners, or those of the specified eventName.
	 *
	 * @param eventName The name of the event
	 * @return a reference to the EventEmitter, so that calls can be chained
	 */
	override fun removeAllListeners(eventName: String): EventEmitterInterface {
		events.get(eventName)!!.clear()
		return this
	}

	/**
	 * Removes all listeners.
	 *
	 * @return a reference to the EventEmitter, so that calls can be chained
	 */
	override fun removeAllListeners(): EventEmitterInterface {
		events.clear()
		return this
	}

	/**
	 * By default EventEmitters will print a warning if more than 10 listeners
	 * are added for a particular event. This is a useful default that helps
	 * finding memory leaks. Obviously, not all events should be limited to
	 * just 10 listeners. The emitter.setMaxListeners() method allows the limit
	 * to be modified for this specific EventEmitter instance.
	 * The value can be set to Infinity (or 0) to indicate an unlimited number of listeners
	 *
	 * @param n maxnumber of listeners
	 * @return a reference to the EventEmitter, so that calls can be chained
	 */
	override fun setMaxListeners(n: Int): EventEmitterInterface {
		defaultMaxListeners = n
		return this
	}

	/**
	 * Get a copy of the array of listeners for the event named eventName.
	 *
	 * @param eventName The name of the event
	 * @return a copy of the array of listeners for the event named eventName.
	 */
	override fun listeners(eventName: String): LinkedList<(args: Array<out Any>) -> Unit> {
		if (events.containsKey(eventName)) {
			val listeners: LinkedList<Listener> = events[eventName]!!
			val listenersRt: LinkedList<(args: Array<out Any>) -> Unit> = LinkedList()
			for (listener in listeners) {
				listenersRt.add(listener.listener)
			}
			return listenersRt
		} else {
			return LinkedList()
		}
	}

	/**
	 * Get the number of listeners listening to the event named eventName
	 *
	 * @param eventName The name of the event
	 * @return the number of listeners listening to the event named eventName.
	 */
	override fun listenerCount(eventName: String): Int {
		if (events.containsKey(eventName)) {
			return events[eventName]!!.size
		} else {
			return 0
		}
	}

	/**
	 * Contain lambda function and status(once,loop)
	 *
	 * @param listener lambda The callback function
	 * @param isOnce status loop(default) or once
	 */
	data class Listener(val listener: (args: Array<out Any>) -> Unit, val isOnce: Boolean = false)
}
