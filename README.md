# mall-study
谷粒学院源码
# 项目环境

## Vagrant部署Centos

Vagrant官网下载即可

> 这里采用中科大的镜像站进行下载
>
> 地址如下：
>
> [Index of /centos-cloud/centos/7/vagrant/x86_64/images/ (ustc.edu.cn)](https://mirrors.ustc.edu.cn/centos-cloud/centos/7/vagrant/x86_64/images/)

```
vagrant init centos7 https://mirrors.ustc.edu.cn/centos-cloud/centos/7/vagrant/x86_64/images/CentOS-7.box
```



**启动虚拟机**

> 启动完之后就可以关掉CMD窗口了，我们用xshell连接即可

```
vagrant up
```



## 虚拟机的配置

### 网络

> 网卡为桥接，这样我们就不用配置端口转发了
>
> virtualbox [虚拟机](https://so.csdn.net/so/search?q=虚拟机&spm=1001.2101.3001.7020)网络配置中对每个网卡都有一个混杂模式的配置，默认为“拒绝”，如此所有进入此接口的报文，如果目的MAC与此接口MAC不相同则全部丢弃。
>
> **由于桥接设备报文转发时，进入接口的报文目的MAC与接口MAC完全不相同，故必须将混杂模式设置为“全部允许”**

![image-20220725165116489](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725165116489.png)

**配置完网络重启网络并进行测试**

![image-20220725165149485](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725165149485.png)

### 登录

vi /etc/ssh/sshd_config 修改 PasswordAuthentication yes

重启服务



### yum源配置

> `-o`参数将服务器的回应保存成文件，等同于`wget`命令。

```bash
$ curl -o example.html https://www.example.com
```

上面命令将`www.example.com`保存成`example.html`。



`-O`参数将服务器回应保存成文件，并将 URL 的最后部分当作文件名。

```bash
curl -O https://www.example.com/foo/bar.html
```

上面命令将服务器回应保存成文件，文件名为`bar.html`。



**使用新 yum 源**

> 阿里云的这个是最快的，网易有点卡
>
> 使用这种方式的前提是网络模式为桥接模式，能直接上网，具体按照前面的进行配置

```
wget -O /etc/yum.repos.d/local.repo http://mirrors.aliyun.com/repo/Centos-7.repo
或者curl下载
curl -o /etc/yum.repos.d/local.repo http://mirrors.aliyun.com/repo/Centos-7.repo

yum clean all && yum makecache

yum install -y epel-release

yum clean all && yum makecache
```

## Docker环境

### yum安装docker

```
第一步
yum install -y yum-utils \
        device-mapper-persistent-data \
        lvm2
第二步使用阿里云镜像
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo && yum makecache fast
第三步安装
yum install docker-ce docker-ce-cli containerd.io
systemctl start docker systemctl enable docker 
docker run hello-world
```

![](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220402204821446.png)

### 配置加速器

![image-20220413165211950](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220413165211950.png)

```
sudo mkdir -p /etc/docker

sudo tee /etc/docker/daemon.json <<-'EOF'
{
"registry-mirrors": ["https://3w352wd.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 安装Mysql

```bash
docker run -p 3306:3306 --name mysql \
-v /mydata/mysql/log:/var/log/mysql \
-v /mydata/mysql/data:/var/lib/mysql \
-v /mydata/mysql/conf:/etc/mysql \
-e MYSQL_ROOT_PASSWORD=root  \
-d mysql:5.7
```

- -p 3306:3306：将容器的3306端口映射到主机的3306端口
- -v /mydata/mysql/conf:/etc/mysql：将配置文件夹挂在到主机
- -v /mydata/mysql/log:/var/log/mysql：将日志文件夹挂载到主机
- -v /mydata/mysql/data:/var/lib/mysql/：将数据文件夹挂载到主机
- -e MYSQL_ROOT_PASSWORD=root：初始化root用户的密码

**MySQL 配置**

```bash
vi /mydata/mysql/conf/my.cnf
[client]
default-character-set=utf8

[mysql]
default-character-set=utf8

[mysqld]
init_connect='SET collation_connection = utf8_unicode_ci'
init_connect='SET NAMES utf8'
character-set-server=utf8
collation-server=utf8_unicode_ci
skip-character-set-client-handshake
skip-name-resolve #跳过域名解析

docker exec -it mysql mysql -uroot -proot
grant all privileges on *.* to 'root'@'%' identified by 'root' with grant option;
flush privileges;

退出，设置开启自动启动
docker update mysql(服务名) --restart=always
```

#### Mysql本地连接失败

- pc和vm能互相ping通
- 关闭firewalld，或放开端口
- 打开ipv4转发
  - vi /etc/sysctl.conf net.ipv4.ip_forward=1    #添加此行配置
  - systemctl restart network && systemctl restart docker
  - sysctl net.ipv4.ip_forward
  - 如果返回为“net.ipv4.ip_forward = 1”则表示修改成功



### 安装Redis

```bash
docker run -p 6379:6379 --name redis -v /mydata/redis/data:/data \
-v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
-d redis redis-server /etc/redis/redis.conf
```

## Git配置

### 配置 Git

> 通过配置git，能让我们提交代码的时候显示我们的在这里设置的名字

```bash
# 配置用户名
git config --global user.name "username" //（名字）
# 配置邮箱
git config --global user.email "username@email.com" //(注册账号时用的邮箱)
```

### 配置 Ssh 免密登录

> 这里-C指定的为邮箱地址

```
ssh-keygen -t rsa -C "mildcaq@163.com"
```

**查看公钥**

```bash
cat ~/.ssh/id_rsa.pub
```

**测试**

```bash
用 ssh -T git@gitee.com 测试登录
成功会出现如下：
Hi 彭于晏! You've successfully authenticated, but GITEE.COM does not provide shell access.
```



### Gitee配置

> gitee为代码托管平台，在这里代码可以更灵活的合作开发、代码回滚等等

![image-20220725210747743](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725210747743.png)

### 从Gitee导入代码到IDEA

![image-20220725221240433](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725221240433.png)



## 建立项目基本架构

![image-20220725221433768](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725221433768.png)

### 提交代码到gitee

![image-20220725221030090](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725221030090.png)



### 初始化数据库

> 这里数据库我们采用docker中部署的mysql

**设置每次重启后自动启动**

```bash
[root@queen ~]# docker update redis --restart=always
redis
[root@queen ~]# docker update mysql --restart=always
mysql
```

**重启虚拟机看是否重新启动**

```bash
[root@queen ~]# docker ps
CONTAINER ID   IMAGE       COMMAND                  CREATED       STATUS              PORTS                                                  NAMES
d82881d71fba   redis       "docker-entrypoint.s…"   5 hours ago   Up About a minute   0.0.0.0:6379->6379/tcp, :::6379->6379/tcp              redis
f8bb0bf0b68a   mysql:5.7   "docker-entrypoint.s…"   5 hours ago   Up About a minute   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp, 33060/tcp   mysql

```

## 快速搭建后台管理系统

> 太强了，这一波操作直接颠覆我的认知好吧，我之前做都是用vue-template这个，不得不说有点麻烦

![image-20220725225125303](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725225125303.png)

### 下载代码

- 通过git clone
  - 这里通过git clone下载的代码我们可以直接下载桌面，然后分别导入之后直接删除即可
- 在gitee直接下载
  - 用IDM下载还是比较快的！

![image-20220725225451061](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725225451061.png)



### 导入后端代码

**1.创建基础架构**

![image-20220726200004929](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220726200004929.png)

**2.引入外部模块**

https://gitee.com/renrenio/renren-fast.git

https://gitee.com/renrenio/renren-fast-vue.git

https://gitee.com/renrenio/renren-generator.git

以上分别是：

- 后端代码
- 前端代码
- 代码生成器



git克隆到桌面，拷贝文件夹到项目文件夹，删除桌面文件

![image-20220726200218101](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220726200218101.png)



**3.导入数据，修改配置文件**

> 这里的数据库都是Docker启动的Mysql

![image-20220725231959015](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220725231959015.png)

**4.修改配置文件**

> 改成 自己的mysql容器地址，还要指定数据库名

```xml
url: jdbc:mysql://192.168.1.12:3306/mall-admin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
```

**5.启动测试**

![image-20220726102408806](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220726102408806.png)

#### 导入项目后的pom问题

>   **` <relativePath></relativePath>`这个作用是不依赖本地parent pom,直接从reposity拉取**
>
>   不继承父类，相当于用自己的

```xml
<parent>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-parent</artifactId>
   <version>2.6.6</version>
   <relativePath></relativePath>
</parent>

在父pom文件，引入新加进来的模块
    <modules>
        <module>mall-product</module>
        <module>mall-coupon</module>
        <module>mall-member</module>
        <module>mall-order</module>
        <module>mall-ware</module>
        <module>mall-common</module>
        <module>renren-fast</module>
    </modules>
```

#### 插件下载失败

> 其中docker的插件版本选择1.2.2可下载

有以下几种方式调试：

- 设置多maven地址
- 设置maven自动导入，删除本地这个包让他重新下载
- 每次更改都要刷新、重启IDEA
- 删除项目，重新克隆到本地

![image-20220726101740975](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220726101740975.png)

#### 更改配置文件，连接测试

```yml
url: jdbc:mysql://192.168.1.12:3306/mall-admin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
username: root
password: root
........
```

启动“renren-admin”，然后访问“http://localhost:8080/renren-fast/”

测试成功如下

![image-20220727204744168](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220727204744168.png)

### 导入前端代码

```
npm config set registry http://registry.npm.taobao.org/
npm install #在下载的前端项目根目录下
npm run dev #启动前端项目
```

如果上面不行可以采用

```
npm install -g cnpm --registry=https://registry.npm.taobao.org
cnpm install node-sass --save
npm run dev #启动前端项目
```

安装成功出现如下提示：

```
DONE  Compiled successfully in 14998ms                 9:25:32

 I  Your application is running here: http://localhost:8001
```

启动测试：

![image-20220726102441069](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220726102441069.png)

至此，简单的后台管理系统搭建完成...

#### npm install失败

开启v2ray等你懂得的软件，没有的话通过

清理缓存：npm rebuild node-sass
              	 npm uninstall node-sass

重新下载：npm i node-sass --sass_binary_site=https://npm.taobao.org/mirrors/node-sass/

再次npm install 直到成功

**还是失败？**

删除node_moudle模块删除重新npm install

快速完成

## 生成所有基本CRUD代码

### 代码生成器注意点

#### controller模板

> 自动生成的controller包中代码带有安全框架shiro，这个我们不需要所以直接注释掉

修改自动生成的模板，注释掉全部的

- 注解：@RequiresPermissions
- 包：import org.apache.shiro.authz.annotation.RequiresPermissions;

![image-20220727220907377](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220727220907377.png)

#### 自定义类的返回路径

因为我们把一些通用返回类异常等都放到了mall-common模块,所以mall-common包的定义要根据代码生成器这个包的路径来写;

比如:同一返回类我们要在写在`主路径.common.utils`包下;

不然全部的controller都会报包的位置错误!

> mainpath就是代码生成器模块配置文件指定的

![image-20220728104616468](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220728104616468.png)**1、git clone克隆到桌面**

![image-20220727204918870](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220727204918870.png)

**2、修改配置**

> 这里拿product模块做示范

```properties
mainPath=com.caq
#\u5305\u540D
package=com.caq.mall
moduleName=product
#\u4F5C\u8005
author=xiaocai
#Email
email=mildcaq@gmail.com
#\u8868\u524D\u7F00(\u7C7B\u540D\u4E0D\u4F1A\u5305\u542B\u8868\u524D\u7F00)
tablePrefix=pms_
```

application.yml

```yml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    #MySQL配置
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.1.12:3306/mall_pms?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

**3、运行模块**

访问：`http://localhost:80/`

![image-20220727205744304](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220727205744304.png)

**4、新建common模块**

> 因为自动生成的代码，需要有同一返回类、统一异常处理....
>
> 这些代码都在renren-fast模块，我们直接拷贝到mall-common模块即可

各模块所需代码如下：

![image-20220727213039350](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220727213039350.png)

依赖如下：

```xml
父pom中引入新家的common模块
<modules>
    <module>mall-product</module>
    <module>mall-coupon</module>
    <module>mall-member</module>
    <module>mall-order</module>
    <module>mall-ware</module>
    <module>renren-fast</module>
    <module>renren-generator</module>
    <module>mall-common</module>
</modules>

common引入如下依赖
<dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.22</version>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpcore</artifactId>
            <version>4.4.13</version>
        </dependency>
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
            <version>2.6</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.17</version>
        </dependency>
    </dependencies>
```

**5、拷贝代码注意点**

打开模块所在文件夹进行拷贝

![image-20220728104041823](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220728104041823.png)

### product模块

**1、引入common模块**

生成的代码都要引入common模块,因为自定义的一些类都指定在了mall-common模块

**2、配置文件**

application.yml

```yml
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://192.168.1.12:3306/mall_pms?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  global-config:
    db-config:
      id-type: auto
  mapper-locations: classpath:/mapper/**/*.xml
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

application.properties

用来配置端口和服务名称

```properties
# 应用名称
spring.application.name=mall-product
# 应用服务 WEB 访问端口
server.port=9999
```

**3、启动类增加mappersan注解**

> 这里强调下：
>
> 代码自动生成器生成的代码中，Mapper文件都加了@Mapper注解，所以我们不用加@MapperScan
>
> 如果你自己写的话，MapperScan是个更好的选择

```
@MapperScan("com.caq.mall.product.dao")
```

**4、测试**

```java
package com.caq.mall;

import com.caq.mall.product.entity.BrandEntity;
import com.caq.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("苹果");
        brandService.save(brandEntity);
    }

}

JDBC Connection [HikariProxyConnection@1371953731 wrapping com.mysql.cj.jdbc.ConnectionImpl@740dcae3] will not be managed by Spring
==>  Preparing: INSERT INTO pms_brand ( name ) VALUES ( ? )
==> Parameters: 苹果(String)
<==    Updates: 1
```

**5、上传代码到github**

> 上传失败可搜索我的文章，今天刚更新的
>
> `Java初学者必看`，这篇文章

![image-20220727230049663](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220727230049663.png)

### coupon模块

**1、修改代码生成器配置文件**

```properties
mainPath=com.caq
#\u5305\u540D
package=com.caq.mall
moduleName=coupon
#\u4F5C\u8005
author=xiaocai
#Email
email=mildcaq@gmail.com
#\u8868\u524D\u7F00(\u7C7B\u540D\u4E0D\u4F1A\u5305\u542B\u8868\u524D\u7F00)
tablePrefix=sms_
```

yml文件

```yml
server:
  port: 80

# mysql
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    #MySQL配置
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.1.12:3306/mall_sms?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

**2、重启服务，重新生成代码文件**

![image-20220728102255561](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220728102255561.png)

**3、拷贝代码，建立coupon配置文件**

> 因为我们是通过SpringInit初始化的模块，所以每个模块都有自带的配置文件applicaiton.properties
>
> 改端口的话我们可以修改applicaiton.properties文件

```yml
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://192.168.1.12:3306/mall_sms?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:/mapper/**/*.xml
  global-config:
    db-config:
      id-type: auto
```

**4、运行，测试**

http://localhost:7000/coupon/coupon/list 

```
{"msg":"success","code":0,"page":{"totalCount":0,"pageSize":10,"totalPage":0,"currPage":1,"list":[]}}
```

### member模块

重复上述操作，改代码生成器配置文件，运行生成代码，拷贝代码到模块

拷贝后的代码，建立配置文件，改端口，运行

测试,没问题即可

![image-20220728110033046](https://typora-1259403628.cos.ap-nanjing.myqcloud.com/image-20220728110033046.png)

### order模块

重复上述操作，改代码生成器配置文件（模块名，数据库名），运行生成代码，拷贝代码到模块

重复上述操作，拷贝后的代码，建立配置文件，改端口，运行















