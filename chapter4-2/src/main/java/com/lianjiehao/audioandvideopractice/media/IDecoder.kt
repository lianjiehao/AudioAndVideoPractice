package com.lianjiehao.audioandvideopractice.media

/**
 * @author :created by tangxianming
 * @date: 2020/6/29
 * @desc: XXX
 */
interface IDecoder : Runnable {

    /**
     * 继续解码
     */
    fun goOn()

    /**
     * 当前帧时间，单位：ms
     */
    fun getCurTimeStamp(): Long

}