package br.com.example.kellmertrack.remote.model

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

data class RemoteResponse<out T>(val status: Status, val data:T?, val message:String?) {
    companion object {
        fun <T> success(data: T?): RemoteResponse<T> {
            return RemoteResponse(Status.SUCCESS, data, null)
        }

        fun <T> error(msg: String, data: T?): RemoteResponse<T> {
            return RemoteResponse(Status.ERROR, data, msg)
        }

        fun <T> loading(data: T?): RemoteResponse<T> {
            return RemoteResponse(Status.LOADING, data, null)
        }
    }

}