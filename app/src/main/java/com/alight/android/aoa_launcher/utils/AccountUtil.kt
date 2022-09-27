package com.alight.android.aoa_launcher.utils

import com.alight.android.aoa_launcher.common.bean.CallVideoBean
import android.util.Log
import com.alight.android.aoa_launcher.net.apiservice.AccountService
import com.alight.android.aoa_launcher.common.bean.TokenManagerException
import com.alight.android.aoa_launcher.common.bean.TokenMessage
import com.alight.android.aoa_launcher.common.bean.TokenPair
import com.alight.android.aoa_launcher.common.i.LauncherListener
import com.alight.android.aoa_launcher.common.i.LauncherProvider
import com.alight.android.aoa_launcher.net.urls.Urls
import com.alight.android.aoa_launcher.net.urls.Urls.BASEURL
import com.google.gson.Gson
import com.google.gson.JsonParser
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.xutils.common.util.LogUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

import java.net.URI


object AccountUtil : LauncherProvider {
    //    const val DSN = "d7f0c2f9-3b2f-4193-9e7d-7fea378d5932"
    private var DSN: String = "808A9F684D009368"  //测试dsn
//    private var DSN: String = SerialUtils.getCPUSerial()
    var isShutdown = false

    override fun getDSN(): String {
        return DSN
    }

    const val LOG_TAG = "token manager"
    var currentUserId: Int? = null
    var tokenMap: MutableMap<Int, TokenPair> = HashMap()
    var retrofit = Retrofit.Builder()
        .baseUrl(Urls.BASEURL)
//        .addConverterFactory(GsonConverterFactory.create())
        .build()


    var service: AccountService = retrofit.create(AccountService::class.java)

    private fun renewToken(tokenPair: TokenPair, blocking: Boolean = true) {
        val call = service.renewToken(tokenPair.token!!)
        if (blocking) {
            val rep = call.execute()
            if (rep.isSuccessful()) {
                val jsonObj = JsonParser.parseString(rep.body()!!.string()).asJsonObject
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
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.w(LOG_TAG, "async renew token fail.Fail(${call.toString()},${t.toString()}")
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: retrofit2.Response<ResponseBody>
                ) {
                    if (response.isSuccessful()) {
                        val jsonObj =
                            JsonParser.parseString(response.body()!!.string()).asJsonObject
                        val cod = jsonObj.get("code").asInt
                        if (cod >= 400) {
                            Log.w(
                                LOG_TAG,
                                "async renew token fail.Fail(${response.code()},${
                                    response.body()?.string()
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
            val jsonObj = JsonParser.parseString(rep.body()?.string()).asJsonObject
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
                "update all token fail.Fail(${rep.code()},${rep.body()?.string()})"
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
            val jsonObj = JsonParser.parseString(rep.body()?.string()).asJsonObject
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
                "update all token fail.Fail(${rep.code()},${rep.body()?.string()})"
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
        //关机时不再调用用户信息接口
        if (!isShutdown)
            declareUser(userId)
    }

    override fun getCurrentUser(): TokenPair {
        return tokenMap[currentUserId!!]!!
    }

    override fun getAllToken(): List<TokenPair> {
        if (!isShutdown)
            updateAllToken()
        return ArrayList<TokenPair>(tokenMap.values)

    }

    override fun getToken(): TokenPair {
        return getValidToken()
    }

    override fun run() {
        Log.i(LOG_TAG, "asf run")
//        SocketIOHandler.initSocketIo()
    }

    override fun register(obj: LauncherListener) {
//        SocketIOHandler.register(obj)
    }

    override fun getQrCode(): ByteArray {
        val rep = service.getQrCode(DSN).execute()
        if (rep.isSuccessful()) {
            return rep.body()!!.bytes()
        } else {
            throw TokenManagerException(
                TokenManagerException.CODE_ERR,
                "get qr code failed"
            )
        }
    }

    override fun getCDK(): String {
        val resp = service.getCDK(DSN).execute()
        if (resp.isSuccessful) {
            var bodyStr = resp.body()?.string();
            LogUtil.d("cdk bodyStr = $bodyStr")
            val jsonObj = JsonParser.parseString(bodyStr).asJsonObject
            val cod = jsonObj.get("code").asInt
            if (cod >= 400) {
                throw Exception(
                    jsonObj.get("msg").asString
                )
            }
            val gson = Gson()
            var data = jsonObj.get("data")
            LogUtil.d("getCDK = $data")
            return data.asString
        } else {
            LogUtil.e(resp.toString())
            throw Exception(resp.message())
        }
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
            val jsonObj = JsonParser.parseString(rep.body()!!.string()).asJsonObject
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
        const val STATE_INIT = 4
        const val STATE_CONNECTTED = 0
        const val STATE_CONNECTING = 1
        const val LOG_TAG = "asf socketio"

        //        private val asfUrl = "https://api.alight-sys.com"
        private val asfUrl = BASEURL

        //        private val asfUrl = "http://192.168.4.92:48001"
        private lateinit var socket: Socket
        private var handler: LauncherListener? = null
        private var state = STATE_INIT
        fun initSocketIo() {
            Log.i(LOG_TAG, "ws init")
            val opts = IO.Options()
            opts.transports = arrayOf("websocket")
            opts.reconnectionAttempts = 5
            opts.reconnectionDelay = 5
            opts.path = "/socket.io/asf/"
            val uri = URI(asfUrl)
            try {
                socket = IO.socket(uri, opts)
                // EVENT_CONNECT_ERROR
                socket.on(Socket.EVENT_DISCONNECT) {
                    onConnectFail()

                }
                socket.on(Socket.EVENT_CONNECT_ERROR) {
                    onConnectFail()
                }
                socket.on(io.socket.client.Socket.EVENT_CONNECT) {
                    onConnect()
                }
                socket.on("ASF.MSG") {
                    Log.d(LOG_TAG, "Incoming message: " + it[0])
                    if (it[0] == null) return@on
                    val callVideoBean = Gson().fromJson(it[0].toString(), CallVideoBean::class.java)
                    val tokenMessage = TokenMessage(
                        callVideoBean.title,
                        callVideoBean.message,
                        callVideoBean.intent_url,
                        callVideoBean.type,
                        mapOf("extra" to callVideoBean.extras)
                    )
                    onMessage(tokenMessage)
                }
                socket.connect()
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.toString())
            }


        }

        fun register(obj: LauncherListener) {
            handler = obj
        }


        // TODO use CAS or Lock
        fun onConnectFail() {
            handler?.onDisconnect()
            if (state == STATE_CONNECTTED) {
                state = STATE_CONNECTING
                Log.e(LOG_TAG, "disconnect")
//                initSocketIo()
            }

        }


        fun onConnect() {
//            Log.e(LOG_TAG,"connect")
            state = STATE_CONNECTTED

            socket.emit(
                "ASF.AOSBindDSN",
                Gson().toJson(
                    mapOf(
                        "title" to "ASF.AOSBindDSN",
                        "message" to getDSN(),
                        "intent_url" to "",
                        "type" to -1,
                        "extras" to ""
                    )
                )
            )

            handler?.onConnect()
        }

        fun onMessage(data: TokenMessage) {
            handler?.onReceive(data)
        }


    }

}

