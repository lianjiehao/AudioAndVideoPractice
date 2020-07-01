package com.lianjiehao.audioandvideopractice

/**
 * @author :created by tangxianming
 * @date: 2020/6/29
 * @desc: XXX
 */
enum class DecodeState {
    //开始解码
    START,

    //解码中
    DECODING,

    //解码暂停
    PAUSE,

    //正在快进
    SEEKING,

    //解码完成
    FINISH,

    //停止解码，解码器释放
    STOP
}