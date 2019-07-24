package config.util;

import com.riozenc.titanTool.properties.Global;
import config.webapp.domain.RestultContent;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);



    //上传文件
    public static String upload(MultipartFile file) {
        //判断系统
        String os = System.getProperty("os.name");
        String sysFile = Global.getConfig("linux.project.path");
        if (os.toLowerCase().startsWith("win")) {
            sysFile = Global.getConfig("win.project.path");
        }
        if (!file.isEmpty()) {
            //绝对路径 采用日期+随机数文件夹名
            String basePath = getFileFolderNew();
            String absolutePath = sysFile+File.separator +Global.getConfig("file.doc.path")+ File.separator + basePath;
            //相对路径
            String relativePath = File.separator +Global.getConfig("file.doc.path")+File.separator+ basePath;

            try {
                // 获取上传的文件名称，并结合存放路径，构建新的文件名称
                String fileName = file.getOriginalFilename();
                //String fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                //地址 D:/titan-file/static/时间_随机数
                File filepath = new File(absolutePath, fileName);
                // 判断路径是否存在，不存在则新创建一个
                if (!filepath.getParentFile().exists()) {
                    filepath.getParentFile().mkdirs();
                }
                file.transferTo(new File(absolutePath + File.separator + fileName));
                return relativePath + File.separator + fileName;
            } catch (IOException e) {
                e.printStackTrace();
                return "300";
            }
        }
        return "404";
    }


    public static String download(String filePath,String fileName) throws Exception{
        File file=null;
        String returnFlag="";
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        HttpURLConnection conn = null;
        FileOutputStream fos=null;
        try {
            //通过http请求访问文件
            String urlPath=filePath.replaceAll(fileName, URLDecoder.decode(fileName,"utf-8"));


            URL url = new URL(urlPath.replaceAll("\\\\", "/"));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-type","application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Charset", "UTF-8");
            InputStream in = conn.getInputStream();

            //下载重命名机制
            String fileInfo = getFileInfo(fileName);

            //建立内存到硬盘的连接
            file=new File(Global.getConfig("win.project.path")
                    +File.separator+fmt.format(new Date())+File.separator+fileInfo);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            fos=new FileOutputStream(file);

            byte[] b=new byte[1024];
            int len=0;
            while((len=in.read(b))!=-1){  //先读到内存
                fos.write(b, 0, len);
            }
            fos.flush();
            returnFlag="200";
        }catch (Exception e){
            e.printStackTrace();
            returnFlag="300";
        }finally {
            if(fos!=null){
                fos.close();
            }
            if(conn!=null){
                conn.disconnect();
            }
            return returnFlag;
        }
    }

    /**
     * 重新命名文件
     * 毫秒数+随机数
     *
     * @return
     */
    public static String getFileFolderNew() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        return fmt.format(new Date()) + "_" + UUID.randomUUID().toString();
    }





    /**
     * @param fileName fileInfo[0]: toPrefix;
     *             fileInfo[1]:toSuffix;
     * @return
     */
    public static String getFileInfo(String fileName) {
        Calendar cal = Calendar.getInstance();
        int index = fileName.lastIndexOf(".");
        String toPrefix = "";
        String toSuffix = "";
        if (index == -1) {
            toPrefix = fileName;
        } else {
            toPrefix = fileName.substring(0, index);
            toSuffix = fileName.substring(index, fileName.length());
        }
        return toPrefix+cal.get(Calendar.HOUR)+cal.get(Calendar.MINUTE)+cal.get(Calendar.SECOND)+toSuffix;
    }


}
