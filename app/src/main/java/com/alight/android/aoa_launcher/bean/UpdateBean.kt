data class UpdateBean(
    val code: Int,
    val data: List<Data>,
    val msg: String,
    val request: String
)

data class Data(
    val apk_md5: String,
    val apk_size: Long,
    val app_force_upgrade: Int,
    val app_name: String,
    val app_url: String,
    val content: String,
    val create_time: String,
    val id: Int,
    val is_active: Boolean,
    val update_time: String,
    val version_code: Int,
    val version_name: String
)