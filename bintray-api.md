#Bintray REST API
##说明 
###访问和版本控制
Bintray REST API使用版本控制，最新版本的API会一直被发布在以下地址：
>https://api.bintray.com

如果访问特定版本的API,可使用版本控制:
>https://bintray.com/api/v1
    
###身份验证
Bintray REST API需要一枚适用的API Key，这个key可以在用户的偏好设置页面获得。
身份验证使用HTTP基本身份验证实现，username就是账户名，密码则是API key
###请求限制
API的请求是有限制的，限制规则如下：这些限制仅仅是针对不属于用户或者用户所在组织的资源。
现在对于每个非付费用户的限制是一天300次请求。
这些限制会被返回在 **X-RateLimit** 的返回头中。比如：     
```
X-RateLimit-Limit:1440 //请求数
X-RateLimit-Remaining:257//剩余数
```
###请求分页
结果有可能是分页。现在返回结果是每次请求50次结果，因此，这个**start_pos**请求参数可以来设置请求的开始位置。
如果使用分页,**X-RangeLimit**参数会被包含在返回头来表示这次请求的结果。
```
GET / . . / . . / . . &start_pos = 250 //设置开始位置为250
X-RangeLimit-Total:20000 //数据总数
X-RangeLimit-StartPos:250 //开始位置
X-RangeLimit-EndPos:299 //数据结束位置
```    
###GPG签名
Bintray支持所有的仓库内容自动签名,包括一些特定的仓库类型的元数据签名，比如：Debian和YUM
所以，至少,这需要一个为个人或组织已经配置号的GPG公共密钥，额外的签名信息，可以保存在Bintray或者通过REST传递。
基本的REST使用的签名信息可能有两种设置方式，浙取决于已经存储在Bintray中的GPG信息。
>1.使用一个X-GPG-PASSPHRASE请求头，当Bintray中的私钥需要密码时。       
```
    X-GPG-PASSPHRASE:passphrase        
```
>2.使用JSON请求体，当需要额外的签名信息时,如:    
    1 替代Bintray GPG公钥  
    2 设置一个可选的私钥,如果私钥不是存储在Bintray    
    3 可选的私钥密码(如果需要)
```
    {
      "subject": "dima",
      "passphrase": "papX***yH8eKw",
      "private_key": "-----BEGIN PGP PRIVATE KEY BLOCK-----"
    }
```
签名信息未提供或不完全则不会对内容进行签名。

##内容下载
###下载内容
```
GET https://dl.bintray.com/:subject/:repo/:file_path
GET https://:subject.bintray.com/:repo/:file_path
```
按照以上链接，从指定的库路径下载内容。
**注意**:下载是通过dl.bintray.com域,或对于付费账户的域:subject.bintray.com。从私人存储库下载内容或下载未发布的文件时，下载只需要使用HTTP BASIC认证。
从私人存储库下载内容或下载未发布的文件时，下载只需要使用HTTP BASIC认证。
**安全**:只有对私有存储库或者私有路径具有“读取”权限的用户。
###动态下载
```
GET /content/:subject/:repo/:file_path?bt_package=:package
```
这个请求链接只能用于Bintray付费的仓库。
下载一个基于动态路径的文件 **file_path**
现在，只支持下载最新($latest)版本,这样有利于下载发布在指定包路径下的最新版本文件
包名可以通过以下方式提供:
   > 作为查询参数，使用bt_package   
     作为矩阵参数，使用bt_package   
   > 在请求头中使用X-Bintray-Package

例如:
```
GET /content/bintray/jcenter/com/jfrog/bintray/client/bintray-client-java-api/$latest/
bintray-client-java-api-$latest.jar
bt_package=bintray-client-java
```
如果请求成功，会返回一个302重定向到一个被自动签名的链接（15s到期）
一个成功的调用将返回一个302重定向到一个生成的URL(15秒到期)签署的文件路径解决。
```
Status: 302 OK
```
###受EULA(最终用户许可协议)保护的产品版本下载
用户必须使用第一次下载之前提供的访问密钥来接受该版本的EULA。尝试在不接受EULA的情况下下载内容将生成通知，其中包含以下响应：“要下载 【资源：工件】，您需要接受其EULA：
http : //bintray.com/ : subject/ product/: product /:artifact.version.name/accept_eula?username=:username“
用户应浏览到指定的URL，使用提供的访问密钥进行登录，然后接受EULA。随后使用相同访问密钥的下载尝试将成功。  
###URL签名
```
POST /signed_url/:subject/:repo/:file_path[?encrypt=:true/false]
```
```
{
    "expiry": 7956915742000,
    "valid_for_secs": 30,
    "callback_id": :id,
    "callback_email": :email,
    "callback_url": :url[?QUERY_PARAM=%callback_id],
    "callback_method": :method,
    "secret": :secret
}
```
    这个资源是只有Bintray高端用户。
    生成一个匿名的,签署下载URL和一个截止日期。
    调用者必须是一个库或者一个出版商的老板组织拥有存储库。
    加密下载是可能的-使用AES加密将256 CBC,看到下面的文档。
    
    以下可以指定输入参数:
    encrypt:可选的。 默认的错误。 将加密设置为true时,将使用给定的秘密(下图)加密响应负载的链接。 如果加密设置为true,没有秘密,Bintray将为您创建一个秘密(推荐)。
    expiry:可选的。 URL的截止日期之后,URL将是无效的, 到期值是在Unix纪元的时间,以毫秒为单位。 默认情况下,如果没有指定到期日将24小时。 相互排斥的,valid_for_secs。
    valid_for_secs:可选的。 以来的秒数代前到期的URL。 相互排斥的,expiry。
    callback_id:可选的。 一个应用标识符的请求。 这个标识符出现在下载日志并在电子邮件和使用下载webhook通知。
    callback_email:可选的。 一个电子邮件地址发送邮件,当用户使用下载的URL。
    这需要一个callback_id。 的callback_id将包含在邮件消息。
    callback_url:可选的。 webhook URL调用当用户已经使用下载的URL。
    callback_method:可选的。 使用HTTP方法进行回调; 将使用后如果未指定的。 支持方法有:GET、POST、PUT和头部。
    secret:可选的。 秘密被用来加密响应负载的联系。
    
    X-Bintray-Secret: The secret to use for decryption - this header will be returned in case encrypt is set to true
    Status: 200 OK
    {
      "url":"https://dl.bintray.com/:subject/:repo/:file_path?expiry=EXPIRY&id=ENCRYPTED_ID&signature=SIGNATURE"
    }
    安全:通过身份验证的用户提供发布的许可,或读/写权限存储库路径
    
    回调识别下载日志中
    
    的callback_id将会出现一个冒号(:)前缀作为用户名字段在一个下载日志的一部分。
    例如:
    
    签署了由匿名用户提供一个回调url下载的id“user254”:
    
    82.102.172.26 -匿名:user254(2014 - 11 - 14 - t23:50:10.207 + 0000]“GET / jfrog / artifactory / artifactory-4。 rpm HTTP / 1.1“200 165294080“https://www.jfrog.com/downloads.html”“Mozilla / 5.0(Windows NT 6.3; WOW64)AppleWebKit / 537.36(KHTML,像壁虎)Chrome / 38.0.2125.111 Safari 537.36”
    签署确认bintray url下载的用户“米”和一个回调的id“user254”:
    
    82.102.172.26 -米:user254(2014 - 11 - 14 - t23:50:10.207 + 0000]“GET / jfrog / artifactory / artifactory-4。 rpm HTTP / 1.1“200 165294080“https://www.jfrog.com/downloads.html”“Mozilla / 5.0(Windows NT 6.3; WOW64)AppleWebKit / 537.36(KHTML,像壁虎)Chrome / 38.0.2125.111 Safari 537.36”
    定期由匿名用户,没有回调url下载id:
    
    82.102.172.26 -匿名(2014 - 11 - 14 - t23:50:10.207 + 0000]“GET / jfrog / artifactory / artifactory-4。 rpm HTTP / 1.1“200 165294080“https://www.jfrog.com/downloads.html”“Mozilla / 5.0(Windows NT 6.3; WOW64)AppleWebKit / 537.36(KHTML,像壁虎)Chrome / 38.0.2125.111 Safari 537.36”
    回调在下载人识别
    
    当使用一个回调webhook载荷将包括一个callback_id字段。
    例如:
    
    Status: 200 OK
    "X-Bintray-Callback-Hmac": "Base64 HMAC-MD5 of :subject/:repo/:file_path keyed by the subject's API key"
    {
      "subject": "my-org",
      "repo": "repo",
      "package": "my-package",
      "version": "1.2.1",
      "file_path": "a/b/release.bin)",
      "callback_id": "malcolm",
      "ip_address": "192.10.2.34"
    }
##内容上传和发布
    
###上传内容
    
```
PUT /content/:subject/:repo/:package/:version/:file_path[?publish=0/1][?override=0/1][?explode=0/1]
```
或者
```
    X-Bintray-Package:包
    X-Bintray-Version:版本
    [X-Bintray-Publish:0/1]
    [X-Bintray-Override:0/1]
    [X-Bintray-Explode:0/1]
``` 
或者:
```
PUT /content/:subject/:repo/:file_path;bt_package=:package;bt_version=:version[;publish=0/1][;override=0/1][;explode=0/1]
```    
上传内容到指定的库路径,包括版本信息(必需)。
包和版本可以通过下列方式指定:
   1. 在请求路径
   2. 作为请求头
   3. 作为矩阵参数
   
可选择将上传的工件发布为上传的一部分（默认为关闭）。其他内容可以在发布日期后的180天内上传到发布的版本。
发布的工件可能会在180天内重新部署; 要覆盖已经发布的工件，您需要指定“override = 1”。越权开关可以通过以下方式之一进行指定：
1. 作为查询参数
2. 作为请求标题
3. 作为矩阵参数
可选地，提供一个**X-Checksum-Sha2**包含客户端sha2校验和的头文件。Bintray将验证给定的sha2，并且在不匹配时将返回一个409冲突错误响应。    
安全性：具有“发布”权限并且经过身份验证的用户，才能读取/写入存储库路径的权利
####自动签名上传
你可以提供一个密码签署上传文件和库元数据使用X-GPG-PASSPHRASE头。 请阅读[GPG签名]来获取额外的内容。
    
###Maven上传
```
PUT /maven/:subject/:repo/:package/:file_path[;publish=0/1]
```
    Maven工件上传到指定的库路径,与包信息(要求)。 版本信息的解决路径,预计跟随Maven布局。 可选直接发布(默认情况下)。
    
    Status: 201 Created
    {
      "message": "success"
    }
    自动签名
    
    你可以提供一个密码签署上传文件使用X-GPG-PASSPHRASE头。 请阅读这额外的细节。
    
    Debian上传
    
    /内容/:/主题:回购/:包/:/版本:file_path;deb_distribution =:分布;deb_component =:组件;deb_architecture =:架构(;发布= 0/1)(;覆盖= 0/1)
    当工件被上传到一个Debian库使用自动索引布局,Debian分布信息是必需的,而且必须被指定。 信息可能被指定使用HTTP矩阵参数(在上面的示例中所示)。 另外,这些信息可以指定使用HTTP请求头:
    
    X-Bintray-Debian-Distribution:<分布>
    X-Bintray-Debian-Component:< >组件
    X-Bintray-Debian-Architecture:<架构>
    Debian-related参数接受多个以逗号分隔的值。 例如:
    
    X-Bintray-Debian-Distribution:老生常谈的
    X-Bintray-Debian-Component:主要
    X-Bintray-Debian-Architecture:i386、amd64
    在这个例子中,计算库元数据上传的。deb将包括“老生常谈的”的“主要”组件分布在两个“i386”和“amd64”架构。
    
    ”:file_path”上传的。deb可能需要的任何值。 例如:
    
    池/主/ m / mypackage_1.0.0_amd64.deb
    自动元数据签名
    
    当上传与“发布= 1”库元数据将自动计算。 元数据自动也可以签署。 你可能提供一个密码为签下库元数据使用X-GPG-PASSPHRASE头, 或触发存储库元数据计算额外签署的细节。
    
    Status: 201 Created
    {
      "message": "success"
    }
    发布/丢弃上传内容
    
    回购/ POST /内容/主题/::包/:/出版版本
    异步发布所有未发表的内容对用户的包的版本。 返回出版文件的数量。
    为了等待发布同步完成并运行这个调用,指定一个"publish_wait_for_secs"在秒超时。 所允许的最大超时等待Bintray使用等的价值-1。 等待的价值0是默认的,异步运行这个电话是一样的没有等待。
    
    可选地,通过在一个"discard"国旗抛弃任何未公开的内容,而不是出版。
    
    自动签名库元数据
    
    库元数据的存储库,支持自动计算(Debian和百胜等), 你可能供应签署所需信息,对json的身体额外的字段。 请阅读这为更多的细节。
    
    {
      ...optional signing details...
      "discard": true,
      "publish_wait_for_secs": -1
    }
    Status: 200 OK
    {
      "files": 39
    }
    对于同步:发布和超时了
    
    Status: 408 Request Timeout
    安全:当发布一个版本,经过身份验证的用户使用“发布”权限,或者版本读/写权限。 当丢弃一个版本,经过身份验证的用户使用“发布”权限,或者库读/写权限。
    
    删除的内容
    
    DELETE /content/:subject/:repo/:file_path
    从指定的库路径删除内容,目前只支持删除文件。
    对于OSS,这个行动是有限的180天从内容的发布日期。
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或读/写权限存储库路径
    
    内容签署
    
    得到GPG公钥
    
    获取用户/用户/:/ / gpg钥匙/ public.key
    获得/组织/:org/keys/gpg/public.key
    主题或组织GPG公钥。
    内容类型的响应格式是“应用程序/ pgp-keys”。
    
    Status: 200 OK
    "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
    " ... " +
    "-----END PGP PUBLIC KEY BLOCK-----"
    GPG签署版本
    
    POST /gpg/:subject/:repo/:package/versions/:version
    GPG签署所有文件与指定的版本。
    GPG签署可能需要的信息。 请参考这为更多的细节。
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或版本读/写权限。
    
    GPG签署一份文件
    
    POST /gpg/:subject/:repo/:file_path
    GPG标志指定的库文件。
    GPG签署可能需要的信息。 请参考这为更多的细节。
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或版本读/写权限。
    
    内容同步
    
    同步版本工件Maven中央
    
    回购/ POST / maven_central_sync /主题/::包/版本:版本
    {
      "username": "userToken", // Sonatype OSS user token
      "password": "passwordToken", // Sonatype OSS user password
      "close": "1" // Optional
    }
    同步版本文件oss.sonatype.org暂存库发布这些文件到Maven中央。
    默认情况下,分段存储库是封闭和工件Maven中央发布。 您可以选择手动关掉这种行为和发布版本。 这是通过传递0“关闭”领域的JSON传递给调用。
    
    Status: 200 OK
    {
      "status": "Successfully synced and closed repo.",
      "messages": ["Sync finished successfully."]
    }
    安全:经过身份验证的用户使用“发布”权限,或者版本读/写权限。
    
    存储库
    
    获取存储库
    
    得到/回购:主题
    获得回购的列表可以由主体(个人或组织)
    
    Status: 200 OK
    [
      {
        "name": "repo",
        "owner": "subject"
      }
    ]
    安全:通过身份验证的用户使用“读取”权限私人仓库或存储库阅读权利。
    
    获取存储库
    
    得到/回购/:/主题:回购
    获取存储库的一般信息的指定的用户
    
    Status: 200 OK
    {
      "name": "repo",
      "owner": "user",
      "type": "maven",
      "private": false,
      "premium": false,
      "version_update_max_days": 60,   (only for Enterprise Account, if defined)
      "desc": "This repo...",
      "labels": ["java", "maven"],
      "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "package_count": 87
      "gpg_sign_metadata": false,
      "gpg_sign_files":false,
      "gpg_use_owner_key":false
    }
    安全:通过身份验证的用户使用“读取”权限私人仓库或存储库阅读权利。
    
    创建存储库
    
    POST /回购:主题/:回购
    创建一个存储库在指定的主题。
    存储库的可能的类型是:maven,debian,柯南,rpm,码头工人,npm,opkg,nuget、流浪汉和通用(默认)。
    
    {
      "name": "repo",
      "type": "maven",
      "private": false,
      "business_unit": "businessUnit1",
      "desc": "This repo...",
      "labels":["label1", "label2"],
      "gpg_sign_metadata": false,
      "gpg_sign_files":false,
      "gpg_use_owner_key":false,
      "version_update_max_days" : 60    (only for Enterprise Account)
    }
    GPG汽车标志旗帜——最后三个国旗在上面的示例中是可选的,他们让你指定GPG签署是否应该应用于这个回购。 汽车签署gpg默认情况下是禁用的。
    
    “business_unit”:一个业务单元可以联系到存储库允许您监控整体的使用/业务单元。
    
    “gpg_sign_metadata”:如果设置为true,那么回购的元数据将自动与Bintray GPG密钥签署。
    
    “gpg_sign_files”:如果设置为true,那么回购的文件将自动与Bintray GPG密钥签署。
    
    “gpg_use_owner_key”:如果设置为true,那么回购的元数据和文件将自动与业主签署的GPG密钥。 这个标志不能同时设置真实的bintray关键赛场(文件或元数据)。 这个标志可以设置真实只有回购的主人提供一个私有和公共GPG密钥bintray概要文件。
    
    “version_update_max_days”:可选的。 数天之后的版本是发表在一个组织成员可以上传,覆盖或删除文件版本,删除或包的版本。 后这段时间可用这些行为不是成员。 这并不适用于存储库的管理员可以在任何时候更改版本后出版
    
    Debian库
    
    可以提供在创建默认的坐标,这些coordintes将用于索引存储库使
    用户存储库包含在消息列表中,同时仍然是空的。
    
    {
       "type":"debian",
       "default_debian_architecture": "amd64",
       "default_debian_distribution": "jessie"
       "default_debian_component": "main"
    }
    的default_debian_component参数是默认的“主要”。
    
    转库
    
    {
      "type": "rpm",
      "yum_metadata_depth": 3,
      "yum_groups_file": "yumGroup.xml" (optional)
    }
    Status: 201 Created
    {
      "name": "repo",
      "owner": "user",
      "type": "maven",
      "private": false,
      "premium": false,
      "business_unit": "businessUnit1",
      "desc": "This repo...",
      labels:["label1","label2"],
      "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "package_count": 0
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    更新存储库
    
    补丁/回购:主题/:回购
    更新存储库下指定的主题
    
    {
      "business_unit": "businessUnit1",
      "desc": "This repo..."
      "labels":["label1","label2"]
      "gpg_sign_metadata": false,
      "gpg_sign_files":false,
      "gpg_use_owner_key":false,
      "version_update_max_days" : 60    (only for Enterprise Account)
    }
    Status: 200 OK
    安全:经过身份验证的用户使用“管理”权限。
    
    删除存储库
    
    删除/回购:主题/:回购
    删除指定的存储库下指定的主题
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    库搜索
    
    GET /搜索/回购? name =:name&desc =:desc
    搜索存储库。 至少有一个名字和desc搜索字段需要指定。 结果返回一个数组,元素类似于单个存储库的结果。
    搜索结果将不含私人存储库。
    
    安全:需要身份验证的用户
    
    链接包
    
    /仓库/:/:回购/链接:source_subject /:source_repo /:source_package
    包source_package到回购库链接。
    调用者必须是一个组织的管理拥有存储库。
    
    有选择地接受JSON的身体路径前缀,包括文件具体路径
    
    {
       "path_prefix": "x/y/z"
    }
    Status: 201 Created
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或存储库读/写权限。
    
    拆开包装
    
    删除/仓库/:/主题:回购/链接:source_subject /:source_repo /:source_package
    拆开包source_package回购的存储库。
    调用者必须是一个组织的管理拥有存储库。
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或存储库读/写权限。
    
    计划元数据计算
    
    POST / calc_metadata /:/主题:回购/(路径):
    计划元数据(指标)计算指定的存储库。 对于Maven存储库需要指定元数据的存储库路径应该计算。 RPM存储库,您需要指定存储库路径根据百胜的元数据文件夹深度的字段,如果不同于零。 其他存储库类型的路径将被忽略。
    
    安全:通过身份验证的用户提供发布的许可,或存储库读/写权限。
    
    自动签名库元数据
    
    对于支持库元数据的存储库签(Debian和百胜等),供应签署必要的信息, 用描述的方法之一在这里。
    
    Status: 202 Accepted
    {
      "message": "Calculation was successfully scheduled for '/:subject/:repo'"
    }
    地理的限制
    
    限制访问存储库基于客户的地理位置。
    这个功能是有限的,用户与企业帐户。
    企业账户没有限制的国家数量中指定他们的白名单或黑名单。
    
    注意:国家代码格式是ISO 3166α2意味着每个国家只定义为2封信。
    例如:美国是美国,加拿大CA等。
    
    获得地理限制
    
    得到/回购/:/:回购/ geo_restrictions
    得到国家的列表中定义“black_list”或“white_list”。
    
    Status: 200 OK
    {
      'white_list' : [US,CA],
      'black_list' : []
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    更新地理限制
    
    把/回购/主题/:回购/ geo_restrictions
    更新“black_list”或“white_list”与国家的相关代码。
    
    注意:更新只能做一个列表。
    
    {
      'white_list' : [US, CA],
      'black_list' : []
    }
    Status: 200 OK
    {
      "The Geo Restrictions for repo path :subject/:repo were updated successfully"
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    删除地理限制
    
    删除/回购/主题/:回购/ geo_restrictions
    删除所有的国家‘white_list’和‘black_list’。
    
    Status: 200 OK
    {
      'white_list' : [],
      'black_list' : []
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    知识产权限制
    
    限制从存储库下载的文件基于源ip。 限制是基于CIDR标记。
    
    获得知识产权的限制
    
    得到回购/:/主题:回购/ ip_restrictions
    白名单和黑名单cidr。
    
    Status: 200 OK
    {
      "white_cidrs": [
        "10.0.0.1/32"
      ],
      "black_cidrs": []
    }
    IP设置限制
    
    回购/:/:回购/ ip_restrictions
    更新知识产权限制,cidr的白名单和黑名单。
    
    {
      "white_cidrs": [
        [
          "10.0.0.1/32",
          "10.0.0.7/32"
        ]
      ],
      "black_cidrs": []
    }
    Status: 200 OK
    {
      "white_cidrs": [
        "10.0.0.1/32"
      ],
      "black_cidrs": []
    }
    更新知识产权限制
    
    补丁回购/:/主题:回购/ ip_restrictions
    添加或删除cidr从黑/白名单限制。
    
    {
      "add": {
        "white_cidrs": [
          "10.0.0.1/32",
          "10.0.0.7/32"
        ],
        "black_cidrs": []
      },
      "remove": {
        "white_cidrs": [
          "10.0.0.9/32",
          "10.0.0.6/24"
        ],
        "black_cidrs": [
          "10.100.0.9/16"
        ]
      }
    }
    Status: 200 OK
    {
      "white_cidrs": [
        "10.0.0.7/32",
        "10.0.0.1/32"
      ],
      "black_cidrs": []
    }
    删除IP的限制
    
    删除回购/:/主题:回购/ ip_restrictions
    移除所有限制,黑色和白色。
    
    Status: 200 OK
    {
      "message": "Successfully deleted restriction for repo /:subject/:repo"
    }
    包
    
    得到包
    
    得到/回购/:/主题:回购/包[? start_pos = 122][&start_name =前缀)
    指定的存储库的包列表,选择指定起始位置和/或一个名字前缀过滤器 这个资源可以被验证和匿名的客户。 为匿名客户将返回不超过100的结果
    
    Status: 200 OK
    [
      {
        "name": "package1",
        "linked": false
      }
    ]
    安全:通过身份验证的用户使用“读取”权限,或库阅读权利。
    
    得到包
    
    GET /包/:主题/:回购/:包(? attribute_value = 1)
    获取指定包和包名的一般信息。
    
    Status: 200 OK
    {
      "name": "my-package",
      "repo": "repo",
      "owner": "user",
      "desc": "This package...",
      "labels": ["persistence", "database"],
      "attribute_names": ["licenses", "vcs", "github", ...], (hidden when using 'attribute_values=1' )
      "licenses": ["Apache-2.0"],
      "custom_licenses": ["my-license-1", "my-license-2"],   (only for Premium Account)
      "followers_count": 82,
      "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "website_url": "http://jfrog.com",
      "rating": 8,
      "issue_tracker_url": "https://github.com/bintray/bintray-client-java/issues",
      "linked_to_repos": [],
      "github_repo": "", (publishers only)
      "github_release_notes_file": "", (publishers only)
      "public_download_numbers": false, (publishers only)
      "public_stats": true, (publishers only)
      "permissions": [],
      "versions": ["0.9", "1.0", "1.0.1", ...],
      "latest_version": "1.2.5",
      "rating_count": 8,
      "system_ids" : [],
      "updated": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "vcs_url": "https://github.com/bintray/bintray-client-java.git",
      "attributes": "{"attr1_name":["attr1_value"], "attr2_name":["attr2_value"]}"   (only when 'attribute_values=1')
    }
    安全:没有进行身份验证的用户。
    
    创建包
    
    POST /包/:/主题:回购
    创建一个新的包中指定的回购(用户必须回购)的所有者
    licenses和vcs_url为OSS包是强制性的。
    的价值licenses需要一组预定义的之一许可证
    custom_licenses只能为溢价账户和支持引用自定义,与主题相关的专利许可拥有包。
    public_stats只有溢价账户是可用的。
    
    {
      "name": "my-package",
      "desc": "This package...",
      "labels": ["persistence", "database"],
      "licenses": ["Apache-2.0", "GPL-3.0"],
      "custom_licenses": ["my-license-1", "my-license-2"],
      "vcs_url": "https://github.com/bintray/bintray-client-java.git",
      "website_url": "http://jfrog.com",
      "issue_tracker_url": "https://github.com/bintray/bintray-client-java/issues",
      "github_repo": "bintray/bintray-client-java",
      "github_release_notes_file": "RELEASE.txt",
      "public_download_numbers": false,
      "public_stats": true
    }
    状态:201 OK
    {包得到JSON响应}
    安全:通过身份验证的用户提供发布的许可,或存储库读/写权限。
    
    删除包
    
    删除/包/:/主题:回购/:包
    删除指定的包
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或存储库读/写权限。
    
    更新包
    
    回购/片/包/:/::包
    更新指定的包的信息。
    的价值licenses需要一组预定义的之一许可证
    custom_licenses只能为溢价账户和支持引用自定义,与主题相关的专利许可拥有包。
    public_stats只有溢价账户是可用的。
    
    {
      "desc": "This package...",
      "labels": ["persistence", "database"],
      "licenses": ["Apache-2.0", "GPL-3.0"],
      "custom_licenses": ["my-license-1", "my-license-2"],
      "vcs_url": "https://github.com/bintray/bintray-client-java.git",
      "website_url": "http://jfrog.com",
      "issue_tracker_url": "https://github.com/bintray/bintray-client-java/issues",
      "github_repo": "bintray/bintray-client-java",
      "github_release_notes_file": "RELEASE_1.2.3.txt",
      "public_download_numbers": false,
      "public_stats": true
    }
    状态:200 OK
    安全:通过身份验证的用户提供发布的许可,或存储库读/写权限。
    
    包搜索
    
    GET /搜索/包(? name =:name&desc =:desc&subject =:subject&repo =:回购)
    搜索一个包。 至少需要指定一个搜索的字段。 主题名称和回购名称必须准确。 结果返回一个数组,元素类似于得到的结果单包。
    搜索结果将不含私人包。
    
    安全:没有进行身份验证的用户。
    
    Maven包搜索
    
    GET /搜索/包/ maven ? g =:groupId&a =:artifactId&q =:查询(主题=:subject&repo =回购):
    搜索一个Maven包使用Maven groupId,artifactId。 至少一个Maven或通配符查询需要指定坐标。 主题名称和回购名称必须准确。
    返回一个数组的结果在以下格式:
    
    {
        "name": "test-package",
        "repo": "jcenter",
        "owner": "bintray",
        "desc": "This package....",
        "system_ids": [
          "groupid:artifactid"
        ],
        "versions": [
            1.0,
            2.0
        ],
        "latest_version": "2.0"
    }
    system_ids字段包含匹配groupId:artifactId坐标在每个发现包。
    搜索结果将不含私人包。
    
    使用示例:
    
    GET https://api.bintray.com/search/packages/maven?g=com.jfrog.bintray.gradle&a=*bintray*&subject=jfrog&repo=jfrog-jars
    或者:
    
    GET https://api.bintray.com/search/packages/maven?q=*bintray*&subject=jfrog&repo=jfrog-jars
    安全:没有进行身份验证的用户。
    
    包文件
    
    得到/ file_package /:/:回购/:filePath
    得到总包存储库文件的信息是相关的。
    返回一个响应得到的结果相似单包。
    
    安全:没有进行身份验证的用户。
    
    版本
    
    获得版本
    
    GET /包/:主题/:回购/:/包/版本:版本(? attribute_value = 1)
    GET /包/:主题/:回购/:包/版本/ _latest(? attribute_value = 1)
    获取指定版本的一般信息,或查询的最新版本至少有一个文件发布到它。
    
    Status: 200 OK
    {
      "name": "1.1.5",
      "desc": "This version...",
      "package": "my-package",
      "repo": "repo",
      "owner": "user",
      "labels": ["OSS", "org-name", ...],
      "published": "true",
      "attribute_names": ["licenses", "vcs", "github", ...],
      "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "updated": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "released": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "github_release_notes_file": "", (publishers only)
      "github_use_tag_release_notes": "", (publishers only)
      "vcs_tag": "", (publishers only)
      "ordinal": 5,
      "attributes": "{"attr1_name":["attr1_value"], "attr2_name":["attr2_value"]}" (only when 'attribute_values=1')
    }
    安全:通过身份验证的用户使用“读取”权限私人存储库或版本阅读权利。
    
    创建版本
    
    回购/ POST /包/:/::包/版本
    指定的包中创建一个新版本的所有者(用户必须包)
    
    {
      "name": "1.1.5",
      "released": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)", (optional)
      "desc": "This version...",
      "github_release_notes_file": "RELEASE.txt", (optional)
      "github_use_tag_release_notes": true, (optional)
      "vcs_tag": "1.1.5" (optional)
    }
    状态:201年创建
    {版本得到JSON响应}
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    删除版本
    
    删除/包/:/主题:回购/:/包/版本:版本
    删除指定的版本
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    更新版本
    
    回购/片/包/:/::包/版本:版本
    指定版本的更新信息
    
    {
      "desc": "This package...",
      "github_release_notes_file": "RELEASE_1.2.3.txt",
      "github_use_tag_release_notes": true,
      "vcs_tag": "1.1.5",
      "released": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)"
    }
    状态:200 OK
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    版本的文件
    
    得到/ file_version /:/:回购/:filePath
    获得一般信息与存储库的版本文件。
    返回一个响应得到的结果相似单一版本。
    
    安全:没有进行身份验证的用户。
    
    产品
    
    这个资源是只有Bintray企业版用户。
    
    获得产品
    
    GET /产品/:主题
    得到的产品列表指定的主题。
    
    Status: 200 OK
    {
      "name": "productName"
    }
    安全:通过身份验证的用户使用“读取”权限私人仓库或存储库阅读权利。
    
    得到的产品
    
    GET /产品/:/:产品
    获得指定的产品的细节。
    
    Status: 200 OK
    {
     "name": "productName",
     "display_name": "productName",
     "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
     "desc": "description",
     "website_url": "http://great-prod.io",
     "vcs_url": "",
     "packages": ["registry/my-docker", "rpms/my-rpm","deb/my-deb"...]
     "versions": ["1.0","1.1"...]
    }
    安全:通过身份验证的用户使用“读取”权限私人仓库或存储库阅读权利。
    
    创建产品
    
    POST /产品/:主题
    创建一个产品对于给定的主题。
    
    {
     "name": "productName",
     "display_name": "productName",
     "desc": "description",
     "website_url": "http://great-prod.io",
     "vcs_url": "",
     "packages": ["registry/my-docker", "rpms/my-rpm","deb/my-deb"...],
     "sign_url_expiry": 10
    }
    状态:201年创建
    身体:JSON的创建产品
    
    安全:经过身份验证的用户使用“管理”权限。
    
    更新产品
    
    补丁/产品/:/主题:产品
    更新现有产品。 必须指定至少一个字段。
    
    {
     "display_name": "productName",
     "desc": "description",
     "website_url": "http://great-prod.io",
     "vcs_url": "",
     "packages": ["registry/my-docker", "rpms/my-rpm","deb/my-deb"...]
    }
    Status: 200 OK
    安全:经过身份验证的用户使用“管理”权限。
    
    删除产品
    
    删除/产品/:/主题:产品
    删除指定的产品及其所有子元素(比如eula)。
    
    Status: 200 OK
    {"message": "deleted"}
    安全:经过身份验证的用户使用“管理”权限。
    
    EULA跟踪
    
    得到产品签署eula
    
    GET /产品/:/:产品/ signed_eulas(? =:fromDate&to =:toDate&username =:username&eula_name =:eula_name]
    获得用户的列表和签署日期,签署了eula /产品版本签署和eula的名字。
    
    以下查询参数可以指定:
    pagination:可选的。
    from和to:可选的。 日期过滤,可以单独提供或在一起,定义在以下ISO8601格式(yyyy-MM-dd没有'HH:mm:ss.SSSZ)。
    username:可选的。 过滤器的用户名。
    eula_name:可选的。 过滤器的Eula的名字。
    
    Status: 200 OK
    {
      "artifactory-pro": [
        {
          "date_signed": "2016-11-03T16:00:32.854Z",
          "username": "joebloggs",
          "product_version_name": "1",
          "eula_name": "artifact_pro_eula"
        },
        .
        .
        .
        {
          "date_signed": "2016-11-01T16:00:32.854Z",
          "username": "jainbloggs",
          "product_version_name": "1",
          "eula_name": "artifact_pro_eula"
        }
      ]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    得到签署eula——所有产品
    
    GET /产品/:主题/ _all signed_eulas(? =:fromDate&to =:toDate&username =:username&eula_name =:eula_name]
    获得用户的列表和签署日期,签署了eula版本签署和eula名称为每个产品由给定的主题。
    
    查询参数解释在这里
    
    Status: 200 OK
    {
      "product_1": [
                        {
                          "date_signed": "2016-11-06T09:22:34.161Z",
                          "username": "btuser1",
                          "product_version_name": "versions-0",
                          "eula_name": "eula1"
                        },
                        {
                          "date_signed": "2016-11-06T09:22:32.585Z",
                          "username": "btuser2",
                          "product_version_name": "versions-1",
                          "eula_name": "eula2"
                        },
                        {
                          "date_signed": "2016-11-06T09:22:30.492Z",
                          "username": "btuser3",
                          "product_version_name": "versions-2",
                          "eula_name": "eula1"
                        }
                   ],
                   .
                   .
                   .
      "product_n": [
                        {
                          "date_signed": "2016-11-06T09:31:33.704Z",
                          "username": "btuser5",
                          "product_version_name": "1.0",
                          "eula_name": "eula8"
                        }
                        {
                          "date_signed": "2016-11-06T09:22:32.585Z",
                          "username": "btuser4",
                          "product_version_name": "versions-1",
                          "eula_name": "eula1"
                        },
                        {
                          "date_signed": "2016-11-06T09:22:30.492Z",
                          "username": "btuser7",
                          "product_version_name": "versions-6",
                          "eula_name": "eula6"
                        }
                   ]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    eula
    
    这个资源是企业只有Bintray用户。
    
    得到eula
    
    GET /产品/:/主题:产品/ eula
    得到的eula列表指定的产品。
    
    Status: 200 OK
    [
      {
       "name": "EULA name",
       "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
       "versions": ["1.0","1.1"...]
      }
    ]
    安全:通过身份验证的用户使用“读取”权限私人仓库或存储库阅读权利。
    
    得到EULA
    
    GET /产品/:/:产品/ eula:eula
    返回指定产品EULA。
    
    Status: 200 OK
    {
     "name": "EULA name",
     "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
     "versions": ["1.0","1.1"...]
     "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
     "content": "..."
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    创建EULA
    
    POST /产品/:/:产品/ eula
    创建一个EULA对于给定的主题,用给定的产品。 一个新的EULA将适用于所有新版本,如果默认的参数被指定为true。
    
    {
     "name": "EULA name",
     "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
     "content": "...",
     "default": false,
     "versions": ["1.0","1.1"...]
    }
    状态:201年创建
    身体:JSON的创建eula
    
    安全:经过身份验证的用户使用“管理”权限。
    
    更新EULA
    
    补丁/产品/:/主题:产品/ eula:eula
    更新下EULA指定主题和产品。 必须指定至少一个字段。
    
    {
     "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
     "content": "...",
     "default": true,
     "versions": ["1.0","1.1"...]
    }
    Status: 200 OK
    安全:经过身份验证的用户使用“管理”权限。
    
    删除EULA
    
    删除/产品/:/主题:产品/ eula:eula
    删除指定的EULA下指定的主题和产品。
    
    Status: 200 OK
    {"message": "deleted"}
    安全:经过身份验证的用户使用“管理”权限。
    
    被文件路径EULA
    
    得到/ file_eula /:/:回购/:file_path
    返回指定的产品工件的EULA路径。
    
    Status: 200 OK
    Body: JSON of the requested <<url_eula_get,eula>>
    安全:经过身份验证的用户使用“管理”权限。
    
    文件
    
    得到包文件
    
    /包/:/主题:回购/:包/文件(? include_unpublished = 0/1)
    得到所有的文件在一个给定的包。
    当调用一个用户提供发布包,权利包括未发表的文件在列表中。 在默认情况下只发表文件所示。
    
    Status: 200 OK
    [
      {
        "name": "nutcracker-1.1-sources.jar",
        "path": "org/jfrog/powerutils/nutcracker/1.1/nutcracker-1.1-sources.jar",
        "package": "jfrog-power-utils",
        "version": "1.1",
        "repo": "jfrog-jars",
        "owner": "jfrog",
        "created": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
        "size": 1234,
        "sha1": "602e20176706d3cc7535f01ffdbe91b270ae5012"
      }
    ]
    安全:通过身份验证的用户使用“读取”权限为私人存储库,或包阅读权利。
    
    获得版本文件
    
    GET /包/:主题/:回购/:/包/版本:版本文件(? include_unpublished = 0/1)
    得到所有的文件在一个特定的版本。
    结果返回一个数组,元素是相似的结果包文件。
    当调用一个用户提供发布包,权利包括未发表的文件在列表中。 在默认情况下只发表文件所示。
    
    安全:通过身份验证的用户使用“读取”权限私人存储库或版本阅读权利。
    
    文件搜索的名字
    
    得到/搜索/文件? name =:名称(主题=:subject&repo =:repo&start_pos =:start_pos
    &created_after = dateCreatedAfter):
    寻找一个文件的名字。 名字可以*和? 通配符。 可能需要一个可选的主题和/或回购的名字的搜索和/或created_after dateCreatedAfter直到今天。 “dateCreatedAfter”定义在以下ISO8601格式(yyyy-MM-dd没有'HH:mm:ss.SSSZ)。 结果返回一个数组,元素是相似的结果包文件。
    搜索结果将不含私人文件。
    
    安全:不需要身份验证
    
    文件搜索的校验和
    
    得到/搜索/文件? sha1 =:sha1[主题=:subject&repo =:repo&start_pos =:start_pos]
    搜索文件的sha1校验和。 可能需要一个可选的主题和/或回购名称搜索。 结果返回一个数组,元素是相似的结果包文件。
    搜索结果将不含私人文件。
    
    安全:不需要身份验证
    
    文件下载列表
    
    把/ file_metadata /主题/:回购/:file_path
    添加或删除一个文件从/到“下载列表”。 通过真实的文件添加到下载列表,和假删除它。
    
    {
      "list_in_downloads":true
    }
    Status: 200 OK
    {"message": "success"}
    安全:通过身份验证的用户提供发布的许可,或版本读/写权限。
    
    自述
    
    得到的自述
    
    GET /包/:主题/:回购/:包/ readme
    返回指定的包的readme。 Bintray readme或GitHub readme。
    
    Status: 200 OK
    {
      "package": "my-package",
      "repo": "repo",
      "owner": "user",
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
      OR:
      "github": {
        "github_repo": "gh_user/repo",
      }
    }
    安全:通过身份验证的用户使用“读取”权限为私人存储库,或包阅读权利。
    
    创建的自述
    
    回购/ POST /包/:/::包/ readme
    创建一个新的指定包的readme。 “内容”必须传递给命令如果使用“bintray”,或将从GitHub库检索,当使用GitHub”。 GitHub库必须提供名称。
    
    {
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
    OR:
      "github": {
        "github_repo": "gh_user/repo",
      }
    }
    Status: 200 CREATED
    {
      "package": "my-package",
      "repo": "repo",
      "owner": "user",
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
      OR:
      "github": {
        "github_repo": "gh_user/repo",
      }
    }
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    创建产品的自述
    
    POST /产品/:/:产品/ readme
    集所有产品的基本包的readme。 也有类似的效果设置包的自述,使用相同的语法。
    
    安全:经过身份验证的用户使用“发布”权限。
    
    删除产品的自述
    
    删除/产品/:/主题:产品/ readme
    删除所有产品的基本包的readme。
    状态:200 OK
    
    安全:经过身份验证的用户使用“发布”权限。
    
    发布说明
    
    发行说明操作可以应用于一个包或版本。
    
    包的发行说明
    
    GET /包/主题/:/ release_notes回购/:包
    返回特定包的发布说明主题; Bintray发布说明或GitHub发行说明。
    
    Status: 200 OK
    {
      "package": "my-package",
      "repo": "repo",
      "owner": "user",
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
      OR:
      "github": {
        "github_repo": "gh_user/repo",
        "github_release_notes_file": "1.2/release_notes.md"
      }
    }
    安全:通过身份验证的用户使用“读取”权限为私人存储库,或包阅读权利。
    
    创建包的发行说明
    
    回购/ POST /包/:/::包/ release_notes
    创建一个包的发布说明主题; 发行说明“内容”必须传递给命令如果使用“bintray”,或将从GitHub提供复制发行说明如果使用GitHub”。 GitHub库必须提供名称。
    版本注释将被应用到所有的版本。
    
    {
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
      OR:
      "github": {
        "github_repo": "gh_user/repo",
        "github_release_notes_file": "1.2/release_notes.md"
      }
    }
    Status: 200 CREATED
    {
      "package": "my-package",
      "repo": "repo",
      "owner": "user",
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
      OR:
      "github": {
        "github_repo": "gh_user/repo",
       "github_release_notes_file": "1.2/release_notes.md"
      }
    }
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    删除包的发行说明
    
    删除/包/主题/:/ release_notes回购/:包
    删除版本说明,主体为一个特定的包。
    
    Status: 200 OK
    {"message":"success"}
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    版本的发布说明
    
    GET /包/:主题/:回购/:/ release_notes包/版本:版本
    返回为一个特定的版本发布说明主题; Bintray发布说明或GitHub发行说明。
    注意:如果使用GitHub,GitHub库名称调用这个命令之前必须提供。 这可以通过UI,或通过调用更新包API。
    
    Status: 200 OK
    {
      "version": "1.2"
      "package": "my-package",
      "repo": "repo",
      "owner": "user",
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
      OR:
      "github": {
        "github_release_notes_file": "1.2/release_notes.md"
        OR:
        "use_release": "true"
      }
     }
    安全:通过身份验证的用户使用“读取”权限私人存储库或版本阅读权利。
    
    创建版本的发行说明
    
    POST /包/:/:回购/:/ release_notes包/版本:版本
    创建一个特定版本的发布说明主题; 发行说明“内容”必须传递给命令如果使用“bintray”,或将从GitHub提供复制发行说明如果使用GitHub”。
    如果通过“github”,通过发行说明文件路径为“github_release_notes_file”,或“真正的”“use_release”使用版本标记为发行说明。
    注意:
    如果使用GitHub,GitHub库名称调用这个命令之前必须提供。 这可以通过UI,或通过调用更新包API。
    如果使用“use_release”,应该配置vcs_tag前调用此命令。 这可以通过UI,或通过调用更新版本API。
    
    {
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
    OR:
      "github": {
        "github_release_notes_file": "1.2/release_notes.md"
        OR:
        "use_release": "true"
      }
    }
    Status: 200 CREATED
    {
      "version": "1.2"
      "package": "my-package",
      "repo": "repo",
      "owner": "user",
      "bintray": {
        "syntax": "markdown", [markdown/asciidoc/plain_text default markdown]
        "content": "the quick brown fox"
      },
      OR:
      "github": {
        "github_release_notes_file": "1.2/release_notes.md"
        OR:
        "use_release": "true"
      }
    }
    安全:通过身份验证的用户提供发布的许可,或版本读/写权限。
    
    属性
    
    得到属性
    
    GET /包/:主题/:回购/:包/属性(? =姓名:att1,att2):
    GET /包/:主题/:回购/:/包/版本:版本/属性(? =姓名:att1,att2):
    获取与指定的包或版本相关的属性。 如果没有指定属性名称,返回所有属性。
    
    Status: 200 OK
    [
      {"name": "att1", "values" : ["val1"], "type": "string"},
      {"name": "att2", "values" : [1, 2.2, 4], "type": "number"},
      {"name": "att3", "values" : ["2011-07-14T19:43:37+0100", "2011-07-14T19:43:37+0100", "1994-11-05T13:15:30Z"], "type": "date"}
    ]
    注:日期以ISO8601定义格式。
    
    安全:通过身份验证的用户使用“读取”权限私人存储库或版本/包阅读权利相应的调用。
    
    设置属性
    
    回购/ POST /包/:/::包/属性
    POST /包/:/:回购/:/包/版本:版本属性
    关联属性和指定的包或版本,覆盖以前的所有属性。 可选地,指定一个属性类型。 否则,类型推断的属性的值。 如果无法推断出类型,将使用字符串类型。 非齐次数组是不会被接受的。 属性名称开头下划线(“_”)为用户提供发布的权利只能是可见的。 属性类型可以是下列之一:字符串,日期,数量,布尔,版本 目前版本像字符串。 这将改变未来Bintray版本。
    
    [
      {"name": "att1", "values" : ["val1"], "type": "string"}, //string
      {"name": "att2", "values" : [1, 2.2, 4]}, //number
      {"name": "att3", "values" : ["2011-07-14T19:43:37+0100", "2011-07-14T19:43:37+0100", "1994-11-05T13:15:30Z"], "type": "date"}, //date
      {"name": "att4", "values" : [1.1, "elephant", 3.1]}, //BAD REQUEST
    ]
    状态:200 OK
    安全:经过身份验证的用户使用“发布”权限私人存储库或版本/包为相应的调用读/写权限。
    
    更新属性
    
    回购/片/包/:/::包/属性
    片/包/:/:回购/:/包/版本:版本/属性
    更新相关的属性指定的包或版本。 属性可能有一个空值。 可选地,指定一个属性类型。 否则,类型推断的属性的值。 如果无法推断出类型,将使用字符串类型。 非齐次数组是不会被接受的。 属性类型可以是下列之一:字符串,日期,数量,布尔,版本 目前版本像字符串。 这将改变未来Bintray版本。
    
    [
      {"name": "att1", "values" : ["val1"], "type": "string"}, //string
      {"name": "att2", "values" : [1, 2.2, 4]}, //number
      {"name": "att3", "values" : ["2011-07-14T19:43:37+0100", "2011-07-14T19:43:37+0100", "1994-11-05T13:15:30Z"], "type": "date"},
      {"name": "att4", "values" : [1.1, "elephant", 3.1]} //string
    ]
    状态:200 OK
    安全:经过身份验证的用户使用“发布”权限私人存储库或版本/包为相应的调用读/写权限。
    
    删除属性
    
    删除/包/:/主题:回购/:包/属性(? =姓名:att1,att2):
    删除/包/:/主题:回购/:/包/版本:版本/属性(? =姓名:att1,att2):
    删除与指定的回购相关的属性,包或版本。 如果没有指定属性名称,删除所有属性
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:经过身份验证的用户使用“发布”权限私人存储库或版本/包为相应的调用读/写权限。
    
    属性搜索
    
    POST /搜索/属性/:/:回购(? attribute_value = 1)
    POST /搜索/属性/:/主题:回购/:包/版本(? attribute_value = 1)
    搜索包/版本在一个给定的存储库匹配的一组属性。 查询和操作符将使用在使用多个条款,例如属性= X和属性B大于Z 当使用一个数组的值,如果现有的属性值是一个标量匹配一个数组的值; 如果现有的属性值是一个数组检查现有的数组包含查询数组。
    
    结果返回一个数组,元素类似于得到的结果单包或者一个单一版本相应地,。
    
    注:括号定义的值范围是方向和逗号的位置。
    
    [
      {"att1" : ["val1", "val2"]},             // att1 == val1 || att1 == val2            (Relevant to STRING only)
      {"att2": "[1,3]"},                       // 1 <= att2 <= 3                          (Relevant to NUMBER only)
      {"att3": "[,3]"},                        // att3 <= 3                               (Relevant to NUMBER only)
      {"att4": "[,3["},                        // att4 < 3                                (Relevant to NUMBER only)
      {"att5": "]2011-07-14T19:43:37+0100,]"}  // att5 value is after 2011-07-14T20:43:37 (Relevant to DATE only)
      (Dates are defined in ISO8601 format.)
    ]
    Status: 200 OK
    [
      {
        package or version object
      },
      {
        package or version object
      }
    ]
    安全:经过身份验证的用户使用“发布”权限私人存储库或版本/包为相应的调用读/写权限。
    
    文件属性
    
    文件属性与指定的主题有关回购和工件。
    每个属性包含三个字段:“名称”、“类型”、“价值观”。 “名称”和“值”是必填字段。
    属性类型可以是下列之一:字符串、日期、数量。 如果JSON不包括属性类型,类型推断的属性的值。 如果无法推断出类型,将使用字符串类型。
    非齐次阵列不被接受,意味着应该相同类型的所有值(字符串、数字、日期)。
    属性名称开头下划线(“_”)为用户提供发布的权利只能是可见的。
    
    注:日期以ISO8601定义格式。
    
    获取文件属性
    
    /文件/:/主题:回购/:file_path /属性
    返回所有与工件相关的属性。 这个资源可以被验证和匿名用户。
    
    Status: 200 OK
    [
      {"name": "att1", "type": "string", "values" : ["val1"]},
      {"name": "att2", "type": "number",  "values" : [1, 2.2, 4] },
      {"name": "att3", "type": "date", "values" : ["2011-07-14T19:43:37+0100",  "2011-07-14T19:43:37+0100", "1994-11-05T13:15:30Z"]}
    ]
    安全:通过身份验证的用户使用“读取”权限,或存储库读权利存储库路径。
    
    设置文件属性
    
    回购/ POST /文件/主题/::file_path /属性
    设置相关的属性指定的工件。 覆盖之前的所有属性。
    
    Status: 200 OK
    [
      {"name": "att1", "type": "string", "values" : ["val1"] }, //string
      {"name": "att2", "values" : [1, 2.2, 4]}, //number
      {"name": "att3", "type": "date", "values" : ["2011-07-14T19:43:37+0100", "2011-07-14T19:43:37+0100", "1994-11-05T13:15:30Z"]}, //date
    ]
    安全:通过身份验证的用户提供发布的许可,或写权利为存储库路径。
    
    更新文件属性
    
    补丁/文件/:/主题:回购/:file_path /属性
    用新属性更新工件没有删除旧构件的属性。
    
    Status: 200 OK
    [
      {"name": "att1", "type": "string", "values" : ["val1"]}, //string
      {"name": "att2", "values" : [1, 2.2, 4]}, //number
      {"name": "att3", “type”:”date”, "values" : ["2011-07-14T19:43:37+0100", "2011-07-14T19:43:37+0100", "1994-11-05T13:15:30Z"]},
    ]
    安全:通过身份验证的用户提供发布的许可,或写权利为存储库路径。
    
    删除文件属性
    
    删除/文件/:/主题:回购/:file_path /属性(? =姓名:attr_name_1,attr_name_2):
    移除与指定工件相关的属性。 默认情况下,删除所有相关的属性指定的工件。 “名字”参数是可选的,用于删除特定属性。
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或写权利为存储库路径。
    
    搜索文件属性
    
    POST /文件/:/主题:回购/搜索/属性
    返回所有工件在指定的库中,至少有一个对应的属性名称和值中指定的JSON载荷。
    
    注:括号定义的值范围是方向和逗号的位置。
    
    [
      {"att1" : ["val1", "val2"]},             // att1 == val1 || att1 == val2            (Relevant to STRING only)
      {"att2": "[1,3]"},                       // 1 <= att2 <= 3                          (Relevant to NUMBER only)
      {"att3": "[,3]"},                        // att3 <= 3                               (Relevant to NUMBER only)
      {"att4": "[,3["},                        // att4 < 3                                (Relevant to NUMBER only)
      {"att5": "]2011-07-14T19:43:37+0100,]"}  // att5 value is after 2011-07-14T20:43:37 (Relevant to DATE only)
      (Dates are defined in ISO8601 format.)
    ]
    Status: 200 OK
    [
      {
        "Name": :file_name,
        “Path”: :file_pat,
        "repo": :repo_name,
        "Package": :package_name,
        "Version": :version,
        "owner": :owner_username,
        "created": :date_created, "size":1,
        "Sha1": :sha1_hash_code,
        "Sha256": :sha256_hash_code
      }
    ]
    安全:通过身份验证的用户使用“读取”权限,或存储库读权利存储库路径。
    
    用户与组织
    
    获取用户
    
    用户/用户/:
    获取指定用户的信息
    
    Status: 200 OK
    {
      "name": "user",
      "full_name": "First M. Last",
      "gravatar_id": "whatever",
      "repos": ["repo1", "repo2"],
      "organizations": ["org1", "org2"],
      "followers_count": 82,
      "registered": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "quota_used_bytes": 55720 (only returned to users with Admin permission)
    }
    安全:需要身份验证的用户
    
    得到组织
    
    得到/组织:组织
    得到指定组织的信息。
    “类型”在“成员”是只提供给组织管理员列表
    “团队”是只提供给高级组织管理员列表
    
    Status: 200 OK
    {
        "name":"jfrog",
        "repos": ["repo1", "repo2"],
        "followers_count":0,
        "registered":"ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
        "owner":"joebloggs",
        "full_name":"JFrog",
        "members":
        [
            {"name": "user1", "type": "owner"},
            {"name": "user2", "type": "member"},
            {"name": "user3", "type": "admin"}
        ],
        "teams":["team1","team2"]
        "quota_used_bytes": 133410 (only returned to users with Admin permission)
    }
    安全:需要身份验证的用户
    
    让追随者
    
    GET /用户/:用户/追随者(? start_pos = 50)
    得到指定的仓库所有者的追随者
    
    Status: 200 OK
    [
      {"name": "user1"},
      {"name": "user2"}
    ]
    安全:需要身份验证的用户
    
    用户搜索
    
    GET /搜索/用户? name =:名字
    搜索用户。 结果返回一个数组,元素类似于单个用户的结果。
    
    安全:需要身份验证的用户
    
    主题
    
    重新生成对象URL签名的密钥
    
    POST /主题:主题/密钥对
    这个资源是只有Bintray高端用户。
    对组织来说,调用者必须是一个组织的管理。
    重新生成URL签署的关键主题。
    注意:再生URL签名的密钥将撤销所有积极签署的URL。
    
    Status: 201 Created
    {
      "message": "success"
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    团队
    
    这个资源是只有Bintray高端用户。
    对组织来说,调用者必须是一个组织的管理。
    
    得到团队
    
    获得/组织/:org/teams
    /用户:用户/团队
    团队与组织相关的列表或一个用户
    
    Status: 200 OK
    {
      "teams":["team1", "team2", "team3"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    得到团队
    
    获得/组织/:org/teams/:team
    GET /用户/用户/团队:团队
    团队与组织相关的细节或一个用户
    
    Status: 200 OK
    {
      "name": "team1",
      "members":["user1", "user2", "user3"],
      "permitted_repos":{"repo1": "admin", "repo2": "publish"}}
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    创建团队
    
    POST /组织/:org/teams
    POST /用户/:用户/团队
    创建一个新的团队对于一个组织或一个用户
    
    {
      "name":"team2",
      "members":["user1", "user2", "user3"]
      "allow_repo_creation": true
      "business_unit": "businessUnit1"     (only for Enterprise Account)
    }
    Status: 201 Created
    {
      "name":"team2",
      "owner":"user4",
      "members":["user1", "user2", "user3"],
      "permitted_repo":[]
      "allow_repo_creation": true
      "business_unit": "businessUnit1"
    }
    “allow_repo_creation”:允许团队成员创建和更新存储库。
    “business_unit”:一个默认的业务单元可以联系到一个团队,并将所有存储库的默认业务单元是由这个团队。 与团队相关业务单位只能如果允许其成员创建存储库。
    
    安全:经过身份验证的用户使用“管理”权限。
    
    更新团队
    
    补丁/组织/:org/teams/:team
    补丁/用户/用户/团队:团队
    更新一个团队与一个组织或一个用户
    
    {
      "members":["user1", "user2"]
      "allow_repo_creation": true
      "business_unit": "businessUnit1"      (only for Enterprise Account)
    }
    Status: 200 OK
    {
      "name":"team1",
      "owner":"user4",
      "members":["user1", "user2"],
      "permitted_repos":[]
      "allow_repo_creation": true
      "business_unit": "businessUnit1" ()
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    删除团队
    
    删除/组织/:org/teams/:team
    删除/用户/用户/团队:团队
    删除一个团队与一个组织或一个用户
    
    Status: 200 OK
    {"message": "success"}
    安全:经过身份验证的用户使用“管理”权限。
    
    让所有的团队权限
    
    得到/回购/:/主题:回购/权限
    得到团队的权限定义指定的存储库
    
    Status: 200 OK
    {
      ["team": "team1", "permission": "read"],
      ["team": "team2", "permission": "publish"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    得到团队的许可
    
    得到/回购/:/主题:回购/权限/:团队
    得到团队的权限定义指定的存储库
    
    Status: 200 OK
    {
      "team":"team1",
      "permission":"publish"
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    团队设置权限
    
    把/回购:主题/:回购/权限
    设置权限为一个团队在定义指定的存储库
    
    {
      "team":"team1",
      "permission":"read"
    }
    Status: 200 OK
    {"message": "success"}
    安全:经过身份验证的用户使用“管理”权限。
    
    删除团队许可
    
    删除/回购/主题/:回购/权限/:团队
    删除指定的权限定义为一个团队存储库
    
    Status: 200 OK
    {"message": "success"}
    安全:经过身份验证的用户使用“管理”权限。
    
    权利
    
    这个资源是只有Bintray Pro和企业用户。
    
    获得钥匙
    
    获得/组织/:org/access_keys
    获取用户/用户/:/ access_keys
    获得访问键的列表与一个组织或一个用户
    
    Status: 200 OK
    {
      "access_keys":["key1", "key2", "key3"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    获得关键
    
    获得/组织/:org/access_keys/:access_key_id
    GET /用户/用户/ access_keys:access_key_id
    得到一个与一个组织或一个用户相关的访问密钥,通过其id。
    
    Status: 200 OK
    {
      "id": "key1",
      "username": "key1@:subject",
      "expiry": 7956915742000,
      "existence_check": {
        "url": "http://callbacks.myci.org/username=:username,password=:password",
        "cache_for_secs": 60
      },
      "white_cidrs": ["127.0.0.1/22", "193.5.0.1/92"],
      "black_cidrs": ["197.4.0.1/4", "137.0.6.1/78"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    创建访问密钥
    
    POST /组织/:org/access_keys
    POST /用户/用户/ access_keys
    创建一个新的访问密钥由一个访问键id,对于一个组织或一个用户。
    一个访问关键password如果未指定将自动生成。
    
    可以指定一个可选的到期,之后访问密钥将被自动撤销。 到期值是在Unix纪元的时间,以毫秒为单位。
    可选地,提供了一个存在检查指令来验证源的身份访问键是否仍然存在。
    存在检查使用回调URL,可以选择使用访问键username和password令牌。 只有当一个404 回调URL返回的访问密钥将被自动删除。
    回调结果缓存指定的段。 的最小值cache_for_secs是60秒。
    另一个选择是提供白色和/或黑色cidr。 指定白色cidr只允许访问那些存在的ip地址范围。 黑色cidr将屏蔽所有存在于指定范围的ip。
    你可以设置api_only假允许访问键访问Bintray UI API。 默认值是正确的。
    
    {
      "id": "key1",
      "expiry": 7956915742000,
      "existence_check": {
        "url": "http://callbacks.myci.org/username=:username,password=:password",
        "cache_for_secs": 60
      },
      "white_cidrs": ["127.0.0.1/22", "193.5.0.1/92"],
      "black_cidrs": ["197.4.0.1/4", "137.0.6.1/78"],
      "api_only": false
    }
    Status: 201 Created
    {
      "username": "key1@:org",
      "password": "8fdf84d2a814783f0fc2ce869b5e7f6ce9f286a0"
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    删除访问密钥
    
    删除/组织/:org/access_keys/:access_key_id
    删除/用户/用户/ access_keys:access_key_id
    删除一个组织或一个用户访问密钥。
    
    Status: 200 OK
    {"message": "success"}
    安全:经过身份验证的用户使用“管理”权限。
    
    更新访问密钥
    
    补丁/组织/:org/access_keys/:access_key_id
    补丁/用户/用户/ access_keys:access_key_id
    更新现有的访问密钥由一个访问密钥id,对于一个组织或一个用户。
    
    password,expiry,existence check,white_cidrs和black_cidrs使用这个补丁命令可以修改。
    一个访问关键password如果指定为空字符串将被自动生成。
    
    {
      "expiry": 7956915742000,
      "existence_check": {
        "url": "http://callbacks.myci.org/username=:username,password=:password",
        "cache_for_secs": 60
      },
      "white_cidrs": ["127.0.0.1/22", "193.5.0.1/92"],
      "black_cidrs": ["197.4.0.1/4", "137.0.6.1/78"]
    }
    Status: 200 OK
    {
      "username": "key1@:org",
      "password": "8fdf84d2a814783f0fc2ce869b5e7f6ce9f286a0"
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    获得权利
    
    得到/回购/:/:回购/津贴
    GET /包/:主题/:回购/:包/津贴
    GET /包/:主题/:回购/:/包/版本:版本/津贴
    得到权利上定义指定的存储库,包或版本。
    
    Status: 200 OK
    {
      ["id": "entitlement1"],
      ["id": "entitlement2"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    获得权利
    
    得到/回购/:/:回购/津贴:entitlement_id
    GET /包/:主题/:回购/:包/津贴:entitlement_id
    GET /包/:主题/:回购/:包/版本/:/福利/版本:entitlement_id
    得到一个id和权利的范围。 范围可以是一个存储库,一个包或版本。
    
    Status: 200 OK
    {
      "id": "7f8d57b16c1046e38062ea3db91838ff77758eca",
      "access": "rw",
      "download_keys": ["key1","key2"],
      "path": "a/b/c",
      "tags": ["tag1", "tag2"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    创造福利
    
    POST /回购/主题/:回购/津贴
    回购/ POST /包/:/::包/津贴
    回购/ POST /包/:/::包/版本:版本/津贴
    在指定创建一种权利范围。 范围可以是一个存储库和一个可选的路径,一个包或版本。
    当指定一个可选的存储库路径值范围,需要相对路径引用一个目录或一个文件存储库中。
    可以访问模式rw(读写:意味着下载、上传和删除)r(阅读:意味着下载)。
    可以添加标签搜索的目的。
    一种权利id如果未指定将自动生成。
    
    {
      "access": "rw",
      "access_keys": ["key1","key2"],
      "path": "a/b/c",
      "tags": ["tag1", "tag2"]
    }
    Status: 201 Created
    {
      "id": "7f8d57b16c1046e38062ea3db91838ff77758eca",
      "path": "a/b/c",
      "access": "rw",
      "access_keys": ["key1","key2"],
      "tags": ["tag1", "tag2"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    删除权利
    
    删除/回购/主题/:回购/津贴:entitlement_id
    删除/包/:/主题:回购/:包/津贴:entitlement_id
    删除/包/:/主题:回购/:包/版本/:/福利/版本:entitlement_id
    删除一个id和权利的范围。 范围可以是一个存储库,一个包或版本。
    
    Status: 200 OK
    {"message": "success"}
    安全:经过身份验证的用户使用“管理”权限。
    
    更新的权利
    
    补丁/回购/主题/:回购/津贴:entitlement_id
    回购/片/包/:/::包/津贴:entitlement_id
    回购/片/包/:/::包/版本:版本/津贴:entitlement_id
    更新的信息指定的指定范围的权利。 范围可以是一个存储库和一个可选的路径,一个包或版本。
    “访问”、“access_keys’,‘access_keys_add’,‘access_keys_remove’和‘标签’使用这个补丁命令可以修改。
    “access_keys”:一组键替换当前列表的访问键。
    “access_keys_add”:一组键添加到当前列表的访问键。
    “access_keys_remove”:键的列表删除当前列表的访问键。
    “标签”:一个列表的标签替换当前的标签列表。
    “tags_add”:标签附加到当前列表的标签列表。
    “tags_remove”:标签的列表删除从当前的标签列表。
    
    {
      "access": "rw",
      "access_keys": ["key1","key2"],
      "tags": ["tag1", "tag2"]
    }
    Status: 200 OK
    {
      "id": "7f8d57b16c1046e38062ea3db91838ff77758eca",
      "path": "a/b/c"
      "access": "rw",
      "access_keys": ["key1","key2"],
      "tags": ["tag1", "tag2"]
    }
    安全:经过身份验证的用户使用“管理”权限。
    
    权利搜索访问密钥
    
    GET /搜索/津贴? access_key =:access_key&scope =:主题[/回购):[/:包][/:版本][深= true / false)
    寻找权利为一个特定的访问密钥在指定的范围。
    最小的范围是一个主题。 您可以添加一个sub-scope回购,包和版本。
    如果深= true(默认是假的),将返回给定的范围下的所有权利,例如,如果范围 库,现有方案和版本权利下将返回给定的存储库。
    
    例如:
    
    GET https://api.bintray.com/search/entitlements?access_key=key1&scope=jfrog
    状态:200 OK
    (
    {
    “id”:“entitlement-id”,
    “回购”:“/ jfrog / test-repo”,
    “路径”:"com/jfrog/bintray/0.1.0”,
    “访问”:“r”,
    “access_keys”(“key1”):
    }
    ]
    安全:经过身份验证的用户使用“管理”权限。
    
    权利搜索标签
    
    GET /搜索/津贴?标签=:tag&scope =:主题[/回购):[/:包][/:版本][深= true / false)
    寻找权利为一个特定的标签在指定的范围内。
    最小的范围是一个主题。 您可以添加一个sub-scope回购,包和版本。
    如果深= true(默认是假的),将返回给定的范围下的所有权利,例如,如果范围 库,现有方案和版本权利下将返回给定的存储库。
    
    例如:
    
    GET https://api.bintray.com/search/entitlements?tag=tag1&scope=jfrog
    状态:200 OK
    (
    {
    “id”:“entitlement-id”,
    “回购”:“/ jfrog / test-repo”,
    “路径”:"com/jfrog/bintray/0.1.0”,
    “访问”:“r”,
    “access_keys”:“key1”,
    “标签”(“标签1”):
    }
    ]
    安全:经过身份验证的用户使用“管理”权限。
    
    统计数据和使用情况报告
    
    所有统计数据api使用日期和时间范围。 在过去的二十四小时,时间分辨率是分钟。 如果提供的日期早于过去24小时,所提供的时间将开始提供日期。
    统计只能获取溢价账户,并且需要发布许可或更高。
    
    获得每日下载
    
    回购/ POST /包/:/::包/统计/ time_range_downloads
    回购/ POST /包/:/::包/版本/:/统计/ time_range_downloads版本
    每日下载数量,通过时间范围,每包或版本。
    
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)"
    }
    Status: 200 OK
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
    For package
      "records":[{"date":"yyyy/MM/dd","downloads":[{"version":"version1","count":4}]},
                 {"date":"yyyy/MM/dd","downloads":[{"version":"version1","count":1},{"version":"version2","count":1}]}]
    For version
      "records":[{"date":"yyyy/MM/dd","downloads":[{"version":"version1","count":4}]},
                 {"date":"yyyy/MM/dd","downloads":[{"version":"version1","count":1}]}]
    }
    安全:经过身份验证的用户使用“发布”权限为私人存储库,或包读/写权限。
    
    得到总下载
    
    回购/ POST /包/:/::包/统计/ total_downloads
    回购/ POST /包/:/::包/版本/:/统计/ total_downloads版本
    下载总数,通过时间范围,每包或版本。
    
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)"
    }
    Status: 200 OK
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
    For package
      "records":[{"version":"version1","count":1},{"version":"version2","count":10}]
    For version
      "records":[{"version":"version1","count":1}]
    }
    安全:经过身份验证的用户使用“发布”权限为私人存储库,或包读/写权限。
    
    被国家下载
    
    回购/ POST /包/:/::包/统计/ country_downloads
    回购/ POST /包/:/::包/版本/:/统计/ country_downloads版本
    被国家的下载数量,通过时间范围,每包或版本。
    
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)"
    }
    Status: 200 OK
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "records":[{"country":"country1","count":1}]
    }
    安全:经过身份验证的用户使用“发布”权限为私人存储库,或包读/写权限。
    
    获得使用情况报告
    
    POST /使用/:主题
    这个资源只能用于Bintray溢价账户。
    
    每月下载和存储使用情况报告,根据指定的日期范围为主题。
    
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)"
    }
    Status: 200 OK
    [
      {
        "from": "2016-04-01T00:00:00.000Z",
        "to": "2016-05-01T00:00:00.000Z",
        "partial_period": false,
        "download_bytes": 190023453467,
        "storage_bytes": 30788543
      },
      {
        "from": "2016-05-01T00:00:00.000Z",
        "to": "2016-05-20T00:00:00.000Z",
        "partial_period": true,
        "download_bytes": 126028851487,
        "storage_bytes": 45762911
      }
    ]
    安全:经过身份验证的用户使用“管理”权限。
    
    获取存储库的使用情况报告
    
    POST /使用/:/主题:回购
    这个资源只能用于Bintray溢价账户。
    
    每月下载和存储使用情况报告,根据指定的日期范围为一个特定的主题库。
    
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)"
    }
    Status: 200 OK
    [
      {
        "business_unit": "businessUnit1",
        "from": "2016-12-01T00:00:00.000Z",
        "to": "2017-01-01T00:00:00.000Z",
        "partial_period": false,
        "download_bytes": 17351146,
        "download_percentage": 8.16136697,
        "storage_bytes": 640048090,
        "storage_percentage": 29.17027826
      },
      {
        "business_unit": "businessUnit1",
        "from": "2017-01-01T00:00:00.000Z",
        "to": "2017-01-20T16:00:32.854Z",
        "partial_period": true,
        "download_bytes": 106386794,
        "download_percentage": 50.04059483,
        "storage_bytes": 533694374,
        "storage_percentage": 24.32319327
      }
    ]
    安全:经过身份验证的用户使用“管理”权限。
    
    获得包使用情况报告
    
    POST /使用/ package_usage /:/主题:回购/(包):[? start_pos = 50)
    这个资源只能用于Bintray溢价账户。
    
    得到当前存储使用情况报告。 报告可以要求指定的存储库,可以选择特定的包。
    
    Status: 200 OK
    [
        {
            "package": "pack1",
            "storage_bytes": 98082192,
            "file_count": 59
        },
        {
            "package": "pack3",
            "storage_bytes": 842052350,
            "file_count": 300
        }
    ]
    安全:通过身份验证的用户使用“管理”权限回购,或“发布”允许特定的包。
    
    被业务部门使用情况报告分组
    
    POST /使用/ business_unit_usage /:主题[/ business_unit):
    这个资源只能用于Bintray企业账户。
    
    每月下载和存储使用情况报告,根据指定的日期范围和分组业务单元。 报告可以请求为特定主题或主题业务单元。
    
    {
      "from": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "to": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)"
    }
    Status: 200 OK
    [
      {
        "business_unit": "businessUnit1",
        "from": "2016-12-01T00:00:00.000Z",
        "to": "2017-01-01T00:00:00.000Z",
        "partial_period": false,
        "download_bytes": 17351146,
        "download_percentage": 8.16136697,
        "storage_bytes": 640048090,
        "storage_percentage": 29.17027826
      },
      {
        "business_unit": "businessUnit1",
        "from": "2017-01-01T00:00:00.000Z",
        "to": "2017-01-20T16:00:32.854Z",
        "partial_period": true,
        "download_bytes": 106386794,
        "download_percentage": 50.04059483,
        "storage_bytes": 533694374,
        "storage_percentage": 24.32319327
      },
      {
        "business_unit": "businessUnit2",
        "from": "2016-12-01T00:00:00.000Z",
        "to": "2017-01-01T00:00:00.000Z",
        "partial_period": false,
        "download_bytes": 88863038,
        "download_percentage": 41.7980382,
        "storage_bytes": 438190535,
        "storage_percentage": 19.97059289
      },
      {
        "business_unit": "businessUnit2",
        "from": "2017-01-01T00:00:00.000Z",
        "to": "2017-01-20T16:00:32.854Z",
        "partial_period": true,
        "download_bytes": 0,
        "download_percentage": 0,
        "storage_bytes": 438190535,
        "storage_percentage": 19.97059289
      }
    ]
    安全:经过身份验证的用户使用“管理”权限。
    
    使用阈值
    
    这个资源只能用于Bintray企业账户。
    
    有三种类型的使用阈值可以设置的组织/仓库/业务单位:
    每月最大存储
    每月下载数量
    每天下载数量
    一旦超过一个阈值,通过的通知将被发送消防带流。
    邮件通知可以发送到组织管理员和其他指定的收件人。
    
    获得使用阈值
    
    GET / usage_threshold /组织/:org
    GET / usage_threshold /回购/:org/:repo
    GET / usage_threshold / business_unit /:org/:business_unit
    Status: 200 OK
    {
        "monthly_storage_bytes": 10000000,
        "monthly_download_bytes": 0,
        "daily_download_bytes": 10000,
        "alert_to_emails": [
          "recipient1@email.com",
          "recipient2@email.com"
        ],
        "alert_to_admins": true
    }
    安全:经过身份验证的用户使用组织的管理权限。
    
    创建使用阈值
    
    POST / usage_threshold /组织/:org
    POST / usage_threshold /回购/:org/:repo
    POST / usage_threshold business_unit /:org/:business_unit
    必须指定至少一个阈值类型。
    
    Status: 201 CREATED
    {
        "monthly_storage_bytes": 10000000,
        "monthly_download_bytes": 10000000,
        "daily_download_bytes": 10000,
        "alert_to_emails": [
          "recipient1@email.com",
          "recipient2@email.com"
        ],
        "alert_to_admins": true (default)/false
    }
    身体:创建JSON的使用门槛
    
    安全:经过身份验证的用户使用组织的管理权限。
    
    更新使用阈值
    
    补丁/ usage_threshold /组织/:org
    补丁/ usage_threshold /回购/:org/:repo
    补丁/ usage_threshold business_unit /:org/:business_unit
    设定一个阈值为0,以禁用特定事件类型的通知。
    
    Status: 200 OK
    {
        "monthly_storage_bytes": 10000000,
        "monthly_download_bytes": 0,
        "daily_download_bytes": 10000,
        "alert_to_emails": [
          "recipient1@email.com",
          "recipient2@email.com"
        ],
        "alert_to_admins": true
    }
    安全:经过身份验证的用户使用组织的管理权限。
    
    删除使用阈值
    
    删除/ usage_threshold /组织/:org
    删除/ usage_threshold /回购/:org/:repo
    删除/ usage_threshold / business_unit /:org/:business_unit
    Status: 200 OK
    安全:经过身份验证的用户使用组织的管理权限。
    
    许可证
    
    这个资源是只有Bintray高端用户。
    
    获得专利许可
    
    获得/组织/:org/licenses
    /用户:用户/许可证
    得到一个定制的列表,专利许可与一个组织或一个用户。
    对组织来说,调用者必须有至少一个存储库出版许可。
    
    Status: 200 OK
    [
      {
        "name": "custom-eula",
        "description":"EULA for product x",
        "url":"https://my-commercial-license.com"
      }
    ]
    安全:经过身份验证的用户使用“发布”权限或更高版本是必需的。 需要“管理”权限的用户
    
    创建专有许可
    
    POST /组织/:org/licenses
    POST /用户/:用户/许可证
    创建一个许可证与组织或一个用户。
    对组织来说,调用者必须是一个组织的管理。
    
    [
      {
        "name": "license1",
        "description":"license-1",
        "url":"https://licenses.com"
      }
    ]
    状态:201年创建
    {许可得到JSON响应}
    安全:经过身份验证的用户使用“管理”权限。
    
    更新专有许可
    
    补丁/组织/:org/licenses/:custom_license_name
    补丁/用户/用户/许可证/:custom_license_name
    更新许可证与组织或一个用户。
    对组织来说,调用者必须是一个组织的管理。
    
    [
      {
        "description":"license-1",
        "url":"https://licenses.com"
      }
    ]
    状态:200 OK
    {许可得到JSON响应}
    安全:经过身份验证的用户使用“管理”权限。
    
    删除专有许可
    
    删除/组织/:org/licenses/:custom_license_name
    删除/用户/用户/许可证:custom_license_name
    删除许可证与组织或一个用户。
    对组织来说,调用者必须是一个组织的管理。
    
    Status: 200 OK
    {"message": "success"}
    安全:经过身份验证的用户使用“管理”权限。
    
    把开源软件许可证
    
    获得/许可证/ oss_licenses
    返回一个列表的所有开源软件许可证。 这个资源可以被验证和匿名的客户。
    
    [
      {
        "name":"Apache-1.0",
        "longname":"The Apache Software License, Version 1.0",
        "url":"http://apache.org/licenses/LICENSE-1.0"
      }
    ]
    状态:200 OK
    日志
    
    这个资源是只有Bintray高端用户。
    
    包下载日志文件列表
    
    GET /包/主题/:回购/:包/日志
    检索可用下载日志文件包的列表
    
    Status: 200 OK
    [
      {
        "name": "downloads-05-11-2013.log.gz",
        "size": 209,
        "update": "2013-11-05T12:55:00Z",
      },
      …
    ]
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    下载包下载日志文件
    
    GET /包/:主题/:回购/:包/日志/:log_name
    下载包下载log_name指定的日志文件
    
    Status: 200 OK
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    消防带流API(事件)
    
    /流/:主题
    得到一个生成的事件流为指定的主题活动。
    目前,支持以下事件类型:download,upload,delete,login_success,login_failure。 响应的身体与分块传输编码标准http(Transfer-Encoding: chunked)。
    它由换行符“\ r \ n”分隔的消息,其中每个消息可以是JSON字符串一行代表一个事件或一个空字符串。 空字符串用于维生。 空keep - alive消息发送每30秒。 客户预计断开/重新连接,如果他们没有收到keep - alive消息60秒钟。
    
    X-Bintray-Stream-Reconnect-Id:响应将包含头X-Bintray-Stream-Reconnect-Id与一个值。 这个值可以用于re-establishinghu连接到相同的事件流。 断开连接的客户端发送连接id应该使用相同的标题(X-Bintray-Stream-Reconnect-Id)。 Bintray将客户端连接到现有的流和流生成任何事件在过去的120秒自流已经断开连接。 这允许客户足够的时间来重建/刷新连接,而不丢失事件。
    预计从客户不要跨不同的连接共享连接id。
    
    压缩可以启用通过提供事件Accept-Encoding: gzip请求头。 在这种情况下,响应压缩与指示Content-Encoding: gzip响应头。
    
    限制:目前,有限制的并发连接3对于一个给定的主题。
    Pleasse注意,可能会有一个120秒的等待时间之间的重新连接尝试当一个连接id是不提供的。 例如:如果3客户已经开始,其中一个是断开+重新没有提供一个连接id,它可能需要等待120秒直到成功建立连接,因为事件是保存在旧的连接时间。
    在太多的情况下连接一个429(太多的请求)响应连接客户将返回错误代码。
    
    安全:身份验证管理。
    
    例子:
    
    HTTP/1.1 200 OK
    {"type":"login_success","time":"2016-12-05T06:32:42.987Z","subject":"user@myorg","ip_address":"72.81.195.4","user_agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36"}
    {"type":"login_failure","time":"2016-12-05T06:33:01.011Z","subject":"user@myorg","ip_address":"72.81.195.4","user_agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36"}
    {"type":"upload","path":"/myorg/mavenp/app.jar","subject":"user","time":"2016-12-05T06:33:49.818Z","ip_address":"72.81.195.4","user_agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36"}
    {"type":"download","path":"/myorg/mavenp/app.jar","subject":"user","time":"2016-12-05T06:34:00.825Z","ip_address":"72.81.195.4","user_agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36","content_length":33465}
    {"type":"delete","path":"/myorg/mavenp/app.jar","subject":"user","time":"2016-12-05T06:34:06.532Z","ip_address":"72.81.195.4","user_agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.98 Safari/537.36"}
    人则
    
    让人
    
    /人/:主题[/回购):
    得到所有人注册为指定的主题,选择特定的存储库。
    
    failure_count是一个回调的次数已经失败了。 一个回调后将auto-deactivated 7后续 失败。 一个成功的回调重置计数。
    
    Status: 200 OK
    [
      {
        "package": "my-package",
        "url": "http://callbacks.myci.org/%r-%p-build",
        "failure_count": "3",
      },
      …
    ]
    安全:通过身份验证的用户使用“读取”权限,或者库读/写权限。
    
    注册一个Webhook
    
    回购/ POST /人/主题/::包
    注册一个webhook释放一个新的包接收通知。 默认情况下,用户可以注册10 webhook回调。 回调URL可能含有r %和% p标记回购和包名,分别。 回调方法请求方法:可以在post、put或获得。 如果不指定,使用post。
    
    {
      "url": "http://callbacks.myci.org/%r-%p-build",
      "method": "post"
    }
    Status: 201 Created
    X-Bintray-WebHookLimit-Limit: 10
    X-Bintray-WebHookLimit-Remaining: 2
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    测试一个Webhook
    
    回购/ POST /人/主题/::包/:版本
    测试webhook调为指定的包发布。 webhook post请求身份验证的HMAC-SHA256认证头包名键控的 注册主体的API键,base64编码。
    
    {
      "url": "http://callbacks.myci.org/%r-%p-build",
      "method": "GET"
    }
    Status: 200 OK
    "X-Bintray-Hook-Hmac": "Base64 HMAC-SHA256 of the package name keyed by the API key."
    {
      "package": "my-package",
      "version": "1.2.1",
      "released": "ISO8601 (yyyy-MM-dd'T'HH:mm:ss.SSSZ)",
      "release_notes": "This is a test" (TBD)
    }
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    删除一个Webhook
    
    删除/人/:/主题:回购/:包
    删除用户的webhook与指定的包中。
    
    Status: 200 OK
    {
      "message": "success"
    }
    安全:通过身份验证的用户提供发布的许可,或包读/写权限。
    
    码头工人
    
    下面的码头工人术语适用于Bintray的术语:
    码头工人注册表实际上是一个Bintray库,能够承载无限的码头工人存储库。 Bintray存储库有一个注册的域名::subject-docker-:repo.bintray.com。
    码头工人库是一个Bintray包,一个码头工人名称空间是翻译的一个前缀Bintray包名称。
    码头工人标签是翻译Bintray版本。
    
    删除码头工人库
    
    删除一个码头工人库可以通过下列选项之一:
    
    使用Bintray删除包rest api
    
    使用码头工人的语法:
    
    DELETE https://:dockerRegistry/v1/repositories/:namespace/:dockerRepository
    例如:
    
    DELETE https://jfrog-docker-registry.bintray.io/v1/repositories/bintray/dockerrepo
    安全:经过身份验证的用户使用“管理”权限。
    
    码头工人删除标签
    
    删除一个码头工人标签可以通过下列选项之一:
    
    使用Bintray删除版本rest api
    
    使用码头工人的语法:
    
    DELETE https://:dockerRegistry/v1/repositories/:namespace/:dockerRepository/tags/:tagName
    例如:
    
    DELETE https://jfrog-docker-registry.bintray.io/v1/repositories/bintray/dockerrepo/tags/1
    安全:通过身份验证的用户提供发布的许可,或存储库读/写权限。
    
    可用的许可证:
    澳式足球联盟afl - 2.1——2.1,AGPL-V3,apache - 1.0,apache - 1.1,apache - 2.0,apl - 1.0,apsl - 2.0,艺术-许可证- 2.0,归因,bouncy castle,BSD,BSD 2-Clause,BSD 3条款,声波测井- 1.0,ca - tosl 1.1 cc0 - 1.0,cddl - 1.0,Codehaus,cpal - 1.0,cpl - 1.0,cpol - 1.02,cuaoffice - 1.0天,Day-Addendum,ECL2,艾菲尔铁塔- 2.0,entessa - 1.0,epl - 1.0,EUDATAGRID,eupl - 1.1,公平,facebook平台,frameworx - 1.0,,gpl - 2.0,gpl - 2.0 + CE,gpl - 3.0,历史、HSQLDB,ibmpl - 1.0,ImageMagick,ipafont - 1.0,ISC,IU-Extreme-1.1.1,JA-SIG,JSON,JTidy,lgpl - 2.1,lgpl - 3.0,Libpng,lppl - 1.0,朗讯- 1.02,米罗,麻省理工学院,Motosoto-0.9.1,mozilla - 1.1,mpl - 2.0,MS-PL,MS-RL,Multics,nasa - 1.3,NAUMEN,Nethack,诺基亚- 1.0 a,nosl - 3.0,国家结核控制规划,NUnit-2.6.3,NUnit-Test-Adapter-2.6.3,oclc - 2.0,openfont - 1.1,Opengroup,OpenSSL,osl - 3.0,php - 3.0,PostgreSQL,公共领域,公共领域——太阳,PythonPL,PythonSoftFoundation,qtpl - 1.0,- 1.0,RicohPL,rpl - 1.5,Scala,simpl - 2.0,Sleepycat,sunpublic - 1.0,sybase - 1.0,TMate,Unlicense,UoI-NCSA,VIM许可证,vovidapl - 1.0,W3C,WTFPL,wxWindows Xnet ZLIB zpl—2.0
    
    ©2017 JFrog有限公司。
    
    
    
    
    
    
    
    收起
