package com.scaleup.kotlingithubbrowser.vo

import com.scaleup.kotlingithubbrowser.vo.Status.*



//class Resource<T>(status: Status, data : T?, message : String?){
//
//    val status = status
//    val data = data
//    val message = message
//
//
//
//
//    companion object {
//
//        fun <T>success(data : T?) : Resource<T>{
//            return Resource(SUCCESS, data, null)
//        }
//
//        fun <T>error(msg : String, data : T?) : Resource<T>{
//            return Resource(ERROR, data, msg)
//        }
//
//        fun <T>loading(data : T?) : Resource<T>{
//            return Resource(LOADING, data, null)
//        }
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as Resource<*>
//
//        if (status != other.status) return false
//        if (data != other.data) return false
//        if (message != other.message) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = status.hashCode()
//        result = 31 * result + (data?.hashCode() ?: 0)
//        result = 31 * result + (message?.hashCode() ?: 0)
//        return result
//    }
//
//
//}

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
class Resource<T>(
                  val status: Status,
                  val data: T?,
                  val message: String?) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val resource = o as Resource<*>?

        if (status !== resource!!.status) {
            return false
        }
        if (if (message != null) message != resource!!.message else resource!!.message != null) {
            return false
        }
        return if (data != null) data == resource.data else resource.data == null
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Resource{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}'
    }

    companion object {

        fun <T> success(data: T?): Resource<T> {
            return Resource(SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(ERROR, data, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(LOADING, data, null)
        }
    }
}
