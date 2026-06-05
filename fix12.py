import os

# 1. AndroidManifest.xml
path = r'F:\EasyApex\app\src\main\AndroidManifest.xml'
with open(path, 'rb') as f:
    raw = f.read()
raw = raw.lstrip(b'\xef\xbb\xbf')
try:
    text = raw.decode('utf-8')
except:
    text = raw.decode('gbk')
if 'REQUEST_INSTALL_PACKAGES' not in text:
    ins = '    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />\n    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="32" />\n    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />\n\n'
    text = text.replace('<application', ins + '<application')
    with open(path, 'wb') as f:
        f.write(text.encode('utf-8'))
    print('1. AndroidManifest.xml updated')
else:
    print('1. AndroidManifest.xml done')

# 2. ApexApi.kt
path = r'F:\EasyApex\app\src\main\java\com\easyapex\ApexApi.kt'
with open(path, 'r', encoding='utf-8') as f:
    api = f.read()
if 'GitHubApiClient' not in api:
    extra = '\n// ================= 4. GitHub Release 数据模型 =================\ndata class GitHubRelease(\n    val tag_name: String,\n    val name: String,\n    val body: String,\n    val html_url: String,\n    val created_at: String,\n    val assets: List<GitHubReleaseAsset>\n)\n\ndata class GitHubReleaseAsset(\n    val name: String,\n    val browser_download_url: String,\n    val size: Int\n)\n\n// ================= 5. GitHub API 客户端 =================\nobject GitHubApiClient {\n    private const val BASE_URL = "https://api.github.com/"\n\n    val api: ApexGithubApi by lazy {\n        Retrofit.Builder()\n            .baseUrl(BASE_URL)\n            .addConverterFactory(GsonConverterFactory.create())\n            .build()\n            .create(ApexGithubApi::class.java)\n    }\n}\n\ninterface ApexGithubApi {\n    /** 获取最新版本 Release 信息 */\n    @GET("repos/easyTIDollar/EasyApex/releases/latest")\n    suspend fun getLatestRelease(): GitHubRelease\n}\n'
    api = api.rstrip() + '\n' + extra + '\n'
    with open(path, 'w', encoding='utf-8') as f:
        f.write(api)
    print('2. ApexApi.kt updated')
else:
    print('2. ApexApi.kt done')

print('Parts 1-2 done')