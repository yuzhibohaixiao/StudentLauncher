package com.alight.android.aoa_launcher.utils

object ListSplitUtil {
    /**
     * 按指定份数，分隔集合，将集合按规定份数分为len个部分
     *
     * @param list
     * @param len
     * @return
     */
    fun <T> averageAssign(source: List<T>, size: Int): List<List<T>> {
        val result = ArrayList<List<T>>()
        var remaider = source.size % size //(先计算出余数)
        val number = source.size / size  //然后是商
        var offset = 0//偏移量
        for (i in 0 until size) {
            var value: List<T>?
            if (remaider > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1)
                remaider--
                offset++
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset)
            }
            result.add(value)
        }
        return result
    }

    /**
     * 按指定大小，分隔集合，将集合按规定个数分为n个部分
     *
     * @param list
     * @param len
     * @return
     */
    fun splitList(list: List<*>?, len: Int): List<List<*>>? {
        if (list == null || list.isEmpty() || len < 1) {
            return null
        }
        val result: MutableList<List<*>> = ArrayList()
        val size = list.size
        val count = (size + len - 1) / len
        for (i in 0 until count) {
            val subList =
                list.subList(i * len, if ((i + 1) * len > size) size else len * (i + 1))
            result.add(subList)
        }
        return result
    }

}