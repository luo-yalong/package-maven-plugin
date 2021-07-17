package com.lyl.plugin;

import com.lyl.utils.PluginUtils;
import com.lyl.utils.ZipUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;

/**
 * @author 罗亚龙
 * @date 2021/7/16 15:18
 */
@Mojo(name = "win")
public class WinMojo extends AbstractMojo {


    /**
     * targetDir="project.build.directory"
     * 结果：D:\project\IDEAProject\BaseJava\test_demo\target
     */
    @Parameter(property = "project.build.directory",required = true)
    private File targetDir;

    /**
     * baseDir="project.basedir"
     * 结果：D:\project\IDEAProject\BaseJava\test_demo
     */
    @Parameter(property = "project.basedir" ,required = true,readonly = true)
    private File baseDir;

    /**
     * sourceDir="project.build.sourceDirectory"
     * 结果：D:\project\IDEAProject\BaseJava\test_demo\src\main\java
     */
    @Parameter(property = "project.build.sourceDirectory" ,required = true,readonly = true)
    private File sourceDir;

    /**
     * testSourceDir="project.build.testSourceDirectory"
     * 结果：D:\project\IDEAProject\BaseJava\test_demo\src\test\java
     */
    @Parameter(property = "project.build.testSourceDirectory" ,required = true,readonly = true)
    private File testSourceDir;

    /**
     * groupId="project.groupId"
     * 结果：com.lyl
     */
    @Parameter(property = "project.groupId" ,required = true)
    private String groupId;

    /**
     * artifactId="project.artifactId"
     * 结果：test_demo
     */
    @Parameter(property = "project.artifactId" ,required = true)
    private String artifactId;

    /**
     * version="project.version"
     * 0.0.1-SNAPSHOT
     */
    @Parameter(property = "project.version" ,required = true)
    private String version;

    /**
     * description="project.description"
     * 结果：Demo project for Spring Boot
     */
    @Parameter(property = "project.description")
    private String description;

    /**
     * arguments="arguments"
     */
    @Parameter(property = "arguments")
    private String[] arguments;

    /**
     * vmOptions="vmOptions"
     * jvm参数
     */
    @Parameter(property = "vmOptions")
    private String vmOptions;

    /**
     * programArguments="programArguments"
     * 自定义参数：例如 --server.port=8080
     */
    @Parameter(property = "programArguments")
    private String programArguments;

    /**
     * 自定义参数：跳过
     */
/*    @Parameter(property = "skip")
    private Boolean skip = false;*/




    @Override
    public void execute(){
        Log log = getLog();

/*        //跳过插件
        if(skip == true){
            log.info("Packages win service are skipped");
            log.info("");
            return;
        }*/

        try {
            //创建临时文件夹
            File distDir = new File(targetDir,File.separator + "dist");
            if(distDir.exists()){
                try {
                    FileUtils.deleteDirectory(distDir);
                } catch (IOException e) {
                    log.error("删除目录失败！请检查文件是否在使用");
                    e.printStackTrace();
                }
            }
            FileUtils.mkdir(distDir.getPath());

            //创建日志文件夹
            File logDir = new File(distDir,File.separator + "logs");
            FileUtils.mkdir(logDir.getPath());

            //拷贝生成zip需要的资源文件
            copyStreamToFile("win/readme.txt", new File(distDir,File.separator + "readme.txt"));
            copyStreamToFile("win/config/service.xml", new File(distDir,File.separator + getJarPrefixName() + ".xml"));
            copyStreamToFile("win/exe/service.exe", new File(distDir,File.separator + getJarPrefixName() + ".exe"));
            PluginUtils.createExeConfig(new File(distDir,File.separator + getJarPrefixName() + ".exe.config"));
            FileUtils.copyFile(new File(targetDir.getPath() + File.separator + getJarName()),new File(distDir , File.separator + getJarName()));

            //将配置参数写入到xml文件中
            convert(new File(distDir.getPath()+File.separator+getJarPrefixName()+".xml"));

            //生成bat脚本
            createBat(distDir, "install.bat", "install");
            createBat(distDir, "uninstall.bat", "uninstall");
            createBat(distDir, "start.bat", "start");
            createBat(distDir, "stop.bat", "stop");
            createBat(distDir, "restart.bat", "restart");

            log.info("开始制作压缩包");
            String zipDir = targetDir.getPath() + File.separator + getJarPrefixName() + ".zip";
            log.info("正在制作压缩包...");
            ZipUtils.zip(distDir.getPath(),zipDir);
            log.info("正在清除临时文件");
            FileUtils.deleteDirectory(distDir);
            log.info("制作完成,文件：" + zipDir);


        } catch (Exception e) {
            log.error("制作 Windows Service 失败" ,e);
            e.printStackTrace();
        }


    }

    /**
     * 将本插件内部resource目录下的资源文件复制到新文件中
     * @param fileName 文件名
     * @param destination 目标文件
     */
    private void copyStreamToFile(String fileName,File destination) throws Exception {
        if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        if (destination.exists() && !destination.canWrite()) {
            String message = "Unable to open file " + destination + " for writing.";
            throw new IOException(message);
        } else {
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(destination);
                IOUtil.copy(in, output);
            } finally {
                IOUtil.close(in);
                IOUtil.close(output);
            }
        }
    }

    /**
     * 属性转化
     * @param xmlFile xml文件
     */
    private void convert(File xmlFile){
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(xmlFile);
            Element root = document.getRootElement();
            root.element("id").setText(artifactId);
            root.element("name").setText(getJarPrefixName());
            root.element("description").setText(null == description ? "暂无描述" : description);
            if (arguments.length > 0) {
                getLog().warn("arguments 参数设置已过期,参数配置可能不会生效,请分别设置 vmOptions 参数 和 programArguments 参数 [https://github.com/JoyLau/joylau-springboot-daemon-windows]");
            }
            String vm_options = StringUtils.isEmpty(vmOptions) ? " " : " " + vmOptions + " ";
            String program_arguments = StringUtils.isEmpty(programArguments) ? "" : " " + programArguments;
            root.element("arguments").setText(vm_options + "-jar " + getJarName() +  program_arguments);
            PluginUtils.saveXml(document,xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param outDri   输出目录
     * @param fileName 文件名
     * @param text     命令文本
     */
    private void createBat(File outDri, String fileName, String text) {
        if (!outDri.exists()) {
            FileUtils.mkdir(outDri.getPath());
        }
        File file = new File(outDri, fileName);
        try (FileWriter w = new FileWriter(file)) {
            w.write("@echo off\n" +
                    "%1 mshta vbscript:CreateObject(\"Shell.Application\").ShellExecute(\"cmd.exe\",\"/c %~s0 ::\",\"\",\"runas\",1)(window.close)&&exit\n" +
                    "%~dp0" + getJarPrefixName() + ".exe " + text + "\n" +
                    "echo The " + getJarPrefixName() + " service current state:\n" +
                    "%~dp0" + getJarPrefixName() + ".exe status\n" +
                    "pause");
        } catch (IOException e) {
//            throw new MojoExecutionException("Error creating file ", e);
            e.printStackTrace();
        }
        // ignore
    }

    /**
     * 获取jar包前缀名
     * @return String
     */
    private String getJarPrefixName() {
        return artifactId + "-" + version;
    }

    /**
     * 获取jar包全名
     * @return String
     */
    private String getJarName() {
        return getJarPrefixName() + ".jar";
    }


}
