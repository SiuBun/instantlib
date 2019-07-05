package com.baselib.instant.util

import android.content.Context

class SomeSingleton private constructor(context: Context) {
    init {
        // Init using context argument
    }

    companion object : SingletonHolder<SomeSingleton, Context>(::SomeSingleton)

}