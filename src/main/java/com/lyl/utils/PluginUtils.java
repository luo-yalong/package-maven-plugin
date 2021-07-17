package com.lyl.utils;

import org.dom4j.Document;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author 罗亚龙
 * @date 2021/7/16 15:32
 */
public class PluginUtils {

    /**
     * 保存 XML 文件
     * @param document 文档
     * @param xmlFile xml文件
     */
    public static void saveXml(Document document, File xmlFile){
        try {
            XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), StandardCharsets.UTF_8));
            writer.write(document);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建exe的配置文件
     * @param destination 目标文件
     * @throws IOException io异常
     */
    public static void createExeConfig(File destination) throws IOException{
        String exeConfigStr = "<configuration>\n" +
                "  <startup>\n" +
                "    <supportedRuntime version=\"v2.0.50727\" />\n" +
                "    <supportedRuntime version=\"v4.0\" />\n" +
                "  </startup>\n" +
                "</configuration>";

        if (destination.getParentFile() != null && !destination.getParentFile().exists()) {
            destination.getParentFile().mkdirs();
        }

        if (destination.exists() && !destination.canWrite()) {
            String message = "Unable to open file " + destination + " for writing.";
            throw new IOException(message);
        } else {
            FileOutputStream output = new FileOutputStream(destination);
            output.write(exeConfigStr.getBytes());
            output.close();
        }
    }



}
