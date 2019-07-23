/**
 * Author : czy
 * Date : 2019年5月17日 下午9:22:14
 * Title : config.webapp.action.FileAction.java
 **/
package config.webapp.action;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import com.riozenc.titanTool.common.json.utils.JSONUtil;
import config.webapp.domain.CommonFile;
import config.webapp.domain.RestultContent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.riozenc.titanTool.properties.Global;

@ControllerAdvice
@RequestMapping("/file")
public class FileAction {
    @RequestMapping(value = "/upload")
    @ResponseBody
    public RestultContent upload(MultipartFile file) throws IllegalStateException, IOException {

        //判断系统
        String os = System.getProperty("os.name");
        String sysFile=Global.getConfig("linux.project.path");
        if (os.toLowerCase().startsWith("win")) {
            sysFile=Global.getConfig("win.project.path");
        }
        RestultContent restultContent = new RestultContent();
        Map<String, String> returnMap = new HashMap<>();
        if (!file.isEmpty()) {
            //绝对路径 采用日期+随机数文件夹名
            String basePath = getFileFolderNew();
            String absolutePath = sysFile + File.separator +
                    Global.getConfig("file.doc.path") + File.separator + basePath;
            //相对路径
            String relativePath = File.separator + Global.getConfig("file.doc.path") + File.separator + basePath;
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
            returnMap.put("relativePath", relativePath + File.separator + fileName);
            restultContent.setStatus(200);
            restultContent.setData(returnMap);
        } else {
            restultContent.setStatus(500);
            restultContent.setErrorMsg("文件不存在");
        }
        return restultContent;
    }

    /**
     * 测试下载
     * 默认保存在D://titan-file//年月日
     * @param fileIson
     * @return
     * @throws Exception
     */
    @RequestMapping("/downLoad")
    public RestultContent fileDownLoad(@RequestBody(required = false) String fileIson) throws Exception {

        CommonFile commonFile = JSONUtil.readValue(fileIson, CommonFile.class);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        FileOutputStream fos=null;
        File file=null;
        HttpURLConnection conn = null;
        String fileName = commonFile.getFileName();
        String realPath = commonFile.getFilePath();//得到文件所在位置
        RestultContent restultContent=new RestultContent();
        try {
            //通过http请求访问文件
            String urlPath=commonFile.getFilePath().replaceAll(fileName,URLDecoder.decode(fileName,"utf-8"));
            URL url = new URL(urlPath.replaceAll("\\\\", "/"));
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-type","application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestMethod("GET");
            InputStream in = conn.getInputStream();
            //建立内存到硬盘的连接
            file=new File(Global.getConfig("win.project.path")
                    +File.separator+fmt.format(new Date())+File.separator+fileName);

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
            restultContent.setStatus(200);
        }catch (Exception e){
            e.printStackTrace();
            restultContent.setStatus(300);
            restultContent.setErrorMsg("下载失败");
        }finally {
            if(fos!=null){
                fos.close();
            }
            if(conn!=null){
                conn.disconnect();
            }
            return restultContent;
        }

    }


    /**
     * 重新命名文件
     * 毫秒数+随机数
     *
     * @return
     */
    public String getFileFolderNew() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        return fmt.format(new Date()) + "_" + UUID.randomUUID().toString();
    }
}
