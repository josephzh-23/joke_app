package com.example.joke_app

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService


// A singleton pattern thread-pool used to handle background task
class AppExecutors {
	private val mNetworkIO = Executors.newScheduledThreadPool(3)

	val td:ScheduledExecutorService
		get() {
			return mNetworkIO
		}


	companion object {
		var instance: AppExecutors? = null
			get() {
				if (field == null) {
					field = AppExecutors()
				}
				return field
			}
			private set
	}
}
