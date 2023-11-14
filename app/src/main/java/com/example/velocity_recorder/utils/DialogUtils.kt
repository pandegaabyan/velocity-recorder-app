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