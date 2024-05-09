package dev.theolm.record

import android.content.Context
import androidx.startup.Initializer

internal lateinit var applicationContext: Context
    private set
public object AppContextContext
public class AppContextInject : Initializer<AppContextContext> {
    override fun create(context: Context): AppContextContext {
        applicationContext = context.applicationContext
        return AppContextContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}