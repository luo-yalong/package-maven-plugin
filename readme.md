# SpringBoot项目打包插件

## 前言
编写本插件的目的是为了将SpringBoot项目的jar包，打包成不同环境中适合的文件。

例如：
    可以将jar包打包成windows服务
    将项目打包成linux环境中直接使用的jar包(包含启动脚本)
    将项目打包成docker中的镜像

## 导入依赖

在项目的pom文件中添加

```xml
<plugin>
    <groupId>com.lyl.plugin</groupId>
    <artifactId>package-maven-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <vmOptions>-Xms64m -Xmx128m -Xmn32m -Xss16m</vmOptions>
        <!--					<vmOptions>-Xms64m -Xmx128m -Xmn32m -Xss16m</vmOptions>-->
        <!--					<programArguments> -server.port=9090</programArguments>-->
    </configuration>
</plugin>
```



## 使用方式
### windows
 > 在项目中已经出现jar包之后，运行`mvn package:win`，可以将生成的jar包打包成Zip文件

``` shell
1. 清除打包文件
mvn clean

2. 打包jar包
mvn package

3. 打包可执行的zip文件
mvn package:win
```



    使用方法：将zip文件复制到windows上面，解压文件，依次点击 install.bat -> start.bat , 此时项目已经成功启动
    
    关闭方法：依次双击 stop.bat -> uninstall.bat 完成关闭服务，卸载服务的过程
    


