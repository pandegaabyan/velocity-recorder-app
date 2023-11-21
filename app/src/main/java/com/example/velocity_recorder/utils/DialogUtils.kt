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

package com.example.velocity_recorder.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.example.velocity_recorder.R


object DialogUtils {

    fun createDialog(
        context: Context,
        title: String? = null,
        message: String? = null,
        positiveAction: String? = null,
        negativeAction: String? = null,
        onSuccessAction: () -> Unit = {},
        onNegativeAction: () -> Unit = {}
    ): Dialog {
        val alertDialogBuilder =
            AlertDialog.Builder(context, R.style.AppDialogTheme)
        if (title != null) alertDialogBuilder.setTitle(title)
        if (message != null) alertDialogBuilder.setMessage(message)
        if (positiveAction != null) alertDialogBuilder.setPositiveButton(
            positiveAction
        ) { _: DialogInterface?, _: Int ->
            onSuccessAction()
        }
        if (negativeAction != null) {
            alertDialogBuilder.setNegativeButton(
                negativeAction
            ) { _: DialogInterface?, _: Int ->
                onNegativeAction()
            }
            alertDialogBuilder.setCancelable(false)
        } else {
            alertDialogBuilder.setCancelable(true)
        }
        return alertDialogBuilder.create()
    }

}