/*
 * Copyright 2022 Prasanna Anbazhagan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paz.velocity_recorder.utils

import android.util.Log
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object ParallelUtils {
    private var cpuCount = Runtime.getRuntime().availableProcessors()

    fun <T> forEach(
        params: Iterable<T>,
        action: (T) -> Unit
    ) {
        val executorService =
            Executors.newFixedThreadPool(cpuCount)
        val futures =
            ArrayList<Future<*>>()
        for (param in params) {
            val future =
                executorService.submit { action(param) }
            futures.add(future)
        }
        for (future in futures) {
            try {
                future.get()
            } catch (ignore: InterruptedException) {
            } catch (ignore: ExecutionException) {
            }
        }
        executorService.shutdown()
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Log.d("AppLog", "failed to execute parallel service, InterruptedException: ${e.stackTrace}")
        }
    }
}