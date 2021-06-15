package com.alight.android.aoa_launcher.utils

import android.util.Log
import com.alight.android.aoa_launcher.apiservice.AccountService
import com.alight.android.aoa_launcher.bean.TokenManagerException
import com.alight.android.aoa_launcher.bean.TokenMessage
import com.alight.android.aoa_launcher.bean.TokenPair
import com.alight.android.aoa_launcher.i.LauncherListener
import com.alight.android.aoa_launcher.i.LauncherProvider
import com.alight.android.aoa_launcher.urls.Urls
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

import java.net.URI


object AccountUtil : LauncherProvider {
    const val DSN = "xxxxxxxxxxxxxxxxxxx" // #todo get dsn m cpu
    const val LOG_TAG = "token manager"
    var currentUserId: Int? = null
    var tokenMap: MutableMap<Int, TokenPair> = HashMap()
    var retrofit = Retrofit.Builder()
        .baseUrl(Urls.ALIGHT_URL)
//        .addConverterFactory(GsonConverterFactory.create())
        .build()

    var service: AccountService = retrofit.create(AccountService::class.java)

    private fun renewToken(tokenPair: TokenPair, blocking: Boolean = true) {
        val call = service.renewToken(tokenPair.token!!)
        if (blocking) {
            val rep = call.execute()
            if (rep.isSuccessful()) {
                val jsonObj = JsonParser.parseString(rep.body()!!.body()!!.string()).asJsonObject
                val cod = jsonObj.get("code").asInt
                if (cod >= 400) {
                    throw TokenManagerException(
                        TokenManagerException.CODE_ERR,
                        jsonObj.get("msg").asString
                    )
                }
                val data = jsonObj.get("data").asJsonObject
                tokenPair.token = data.get("ACToken").asString
                tokenPair.expireTime = data.get("expire_time").asDouble
            }
        } else {
            call.enqueue(object : Callback<Response> {
                override fun onFailure(call: Call<Response>, t: Throwable) {
                    Log.w(LOG_TAG, "async renew token fail.Fail(${call.toString()},${t.toString()}")
                }

                override fun onResponse(
                    call: Call<Response>,
                    response: retrofit2.Response<Response>
                ) {
                    if (response.isSuccessful()) {
                        val jsonObj =
                            JsonParser.parseString(response.body()!!.body()!!.string()).asJsonObject
                        val cod = jsonObj.get("code").asInt
                        if (cod >= 400) {
                            Log.w(
                                LOG_TAG,
                                "async renew token fail.Fail(${response.code()},${
                                    response.body()?.body()?.string()
                                }"
                            )
                            return
                        }
                        val data = jsonObj.get("data").asJsonObject
                        tokenPair.token = data.get("token").asString
                        tokenPair.expireTime = data.get("expire_time").asDouble
                    }
                }
            })
        }
    }

    private fun updateAllToken() {
        val newMap = HashMap<Int, TokenPair>() as MutableMap<Int, TokenPair>
        val rep = service.getRelatedUsers(DSN).execute()
        if (rep.isSuccessful()) {
            val jsonObj = JsonParser.parseString(rep.body()!!.body()!!.string()).asJsonObject
            val cod = jsonObj.get("code").asInt
            if (cod >= 400) {
                throw TokenManagerException(
                    TokenManagerException.CODE_ERR,
                    jsonObj.get("msg").asString
                )
            }
            val gson = Gson()
            jsonObj.get("data").asJsonObject.get("users").asJsonArray.forEach {
                val obj = gson.fromJson(it, TokenPair::class.java)
                newMap[obj.userId] = obj
            }
            tokenMap = newMap
        } else {
            throw TokenManagerException(
                TokenManagerException.CODE_ERR,
                "update all token fail.Fail(${rep.code()},${rep.body()?.body()?.string()})"
            )
        }
    }

    private fun declareUser(userId: Int) {
        val tokenPair = getValidToken(userId)
        val rep = service.declareUser(
            tokenPair.token!!, RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                mapOf(
                    "user_id" to userId,
                    "dsn" to DSN
                ).toJson()
            )
        ).execute()
        if (rep.isSuccessful()) {
            val jsonObj = JsonParser.parseString(rep.body()!!.body()!!.string()).asJsonObject
            val cod = jsonObj.get("code").asInt
            if (cod >= 400) {
                throw TokenManagerException(
                    TokenManagerException.CODE_ERR,
                    jsonObj.get("msg").asString
                )
            }
            currentUserId = userId
        } else {
            throw TokenManagerException(
                TokenManagerException.CODE_ERR,
                "update all token fail.Fail(${rep.code()},${rep.body()!!.body()!!.string()})"
            )
        }
    }

    private fun getValidToken(userId: Int): TokenPair {
        return tokenMap.get(userId)?.apply {
            val curTime = System.currentTimeMillis().toDouble() / 1000
            // may cause token invalid,so maybe update must be blocking
            if (expireTime!! <= curTime) {
                renewToken(this)
            } else if (expireTime!! - curTime < 60 * 2) {
                renewToken(this, blocking = false)
            }
        } ?: throw TokenManagerException(TokenManagerException.CODE_ERR, "$userId not valid")
    }

    fun getValidToken(): TokenPair {
        if (currentUserId != null) {
            return getValidToken(currentUserId!!)
        } else {
            throw TokenManagerException(TokenManagerException.CODE_ERR, "not declare login user")
        }
    }

    override fun selectUser(userId: Int) {
        declareUser(userId)
    }

    override fun getCurrentUser(): TokenPair {
        return tokenMap[currentUserId!!]!!
    }

    override fun getAllToken(): List<TokenPair> {
        updateAllToken()
        return ArrayList<TokenPair>(tokenMap.values)

    }

    override fun getToken(): TokenPair {
        return getValidToken()
    }

    override fun getDSN(): String {
        return DSN
    }

    override fun run() {
        SocketIOHandler.initSocketIo()
    }

    override fun register(obj: LauncherListener) {
        SocketIOHandler.register(obj)
    }

    override fun postMessage(message: TokenMessage) {
        val tk = getValidToken()
        val rep = service.postMsg(
            tk.token!!, RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                message.toJson()
            )
        ).execute()
        if (rep.isSuccessful()) {
            val jsonObj = JsonParser.parseString(rep.body()!!.body()!!.string()).asJsonObject
            val cod = jsonObj.get("code").asInt
            if (cod >= 400) {
                throw TokenManagerException(
                    TokenManagerException.CODE_ERR,
                    jsonObj.get("msg").asString
                )
            }
        } else {
            throw TokenManagerException(
                TokenManagerException.CODE_ERR,
                "call failed"
            )
        }
    }

    object SocketIOHandler {
        const val LOG_TAG = "asf socketio"
        private val asfUrl = "https://xxxxxx" //todo get it from config
        private lateinit var socket: Socket
        private var handler: LauncherListener? = null
        fun initSocketIo() {

            val opts = IO.Options()
            opts.transports = arrayOf("websocket")
            opts.reconnectionAttempts = 5
            opts.reconnectionDelay = 5
            val uri = URI(asfUrl)
            try {
                socket = IO.socket(uri, opts)
                socket.on(Manager.EVENT_RECONNECT_FAILED) {
                    onConnectFail()

                }
                socket.on(io.socket.client.Socket.EVENT_CONNECT) {
                    onConnect()
                }
                socket.on("ASF.MSG") {
                    Log.d(LOG_TAG, "Incoming message: " + it[0])
                    onMessage(it.get(0) as TokenMessage)
                }
                socket.connect()
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.toString())
            }


        }

        fun register(obj: LauncherListener) {
            handler = obj
        }

        fun onConnectFail() {
            handler?.onDisconnect()
            initSocketIo()
        }

        fun onConnect() {
            socket.emit(
                "ASF.AOSBindDSN", mapOf(
                    "title" to "ASF.AOSBindDSN",
                    "message" to getDSN(),
                    "intent_url" to "",
                    "type" to -1,
                    "extras" to ""
                )
            )
            handler?.onConnect()
        }

        fun onMessage(data: TokenMessage) {
            handler?.onReceive(data)
        }


    }

}

