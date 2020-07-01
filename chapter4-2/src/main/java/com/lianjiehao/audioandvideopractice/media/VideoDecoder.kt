package com.lianjiehao.audioandvideopractice.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import com.lianjiehao.audioandvideopractice.utils.logError
import java.nio.ByteBuffer

/**
 * @author :created by tangxianming
 * @date: 2020/6/29
 * @desc: XXX
 */
class VideoDecoder(
    path: String,
    private var surface: Surface?,
    val callback: (videoWidth: Int, videoHeight: Int) -> Unit
) : BaseDecoder(
    path
) {

    override fun check(): Boolean {
        if (surface == null) {
            logError("surface为空")
            return false
        }
        return true
    }

    override fun initExtractor(path: String): IExtractor {
        return VideoExtractor(path)
    }

    override fun initSpecParams(format: MediaFormat) {
        callback.invoke(
            format.getInteger(MediaFormat.KEY_WIDTH),
            format.getInteger(MediaFormat.KEY_HEIGHT)
        )
    }

    override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
        codec.configure(format, surface, null, 0)
        return true
    }

    override fun initRender(): Boolean {
        return true
    }

    override fun render(
        outputBuffers: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
    }

    override fun doneDecode() {
    }
}