package com.lianjiehao.audioandvideopractice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OpenGLES20Activity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(MyGLSurfaceView(this))
    }
}
