/**
 * Author : czy
 * Date : 2019年5月17日 下午9:22:14
 * Title : config.webapp.action.FileAction.java
 **/
package config.webapp.action;

import com.riozenc.titanTool.common.json.utils.JSONUtil;
import com.riozenc.titanTool.properties.Global;
import config.util.FileUtil;
import config.webapp.domain.CommonFile;
import config.webapp.domain.RestultContent;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ControllerAdvice
@RequestMapping("/file")
public class FileAction {

    //文件上传
    @RequestMapping(value = "/upload")
    @ResponseBody
    public RestultContent upload(MultipartFile file) throws IllegalStateException, IOException {

        RestultContent restultContent = new RestultContent();
        Map<String, String> returnMap = new HashMap<>();
        String returnFlag = FileUtil.upload(file);
        if ("404".equals(returnFlag)) {
            restultContent.setStatus(300);
            restultContent.setErrorMsg("文件不存在");
        } else if ("300".equals(returnFlag)) {
            restultContent.setStatus(300);
            restultContent.setErrorMsg("文件上传失败");
        } else {
            returnMap.put("relativePath", returnFlag);
            restultContent.setStatus(200);
            restultContent.setData(returnMap);
        }
        return restultContent;
    }

    /**
     * 测试下载
     * 默认保存在D://titan-file//年月日
     *
     * @param fileIson
     * @return
     * @throws Exception
     */
    @RequestMapping("/downLoad2")
    @ResponseBody
    public RestultContent fileDownLoad2(@RequestBody(required = false) String fileIson) throws Exception {
        System.out.println("===============开始下载");
        System.out.println("===============下载参数为" + fileIson);
        RestultContent restultContent = new RestultContent();
        CommonFile commonFile = JSONUtil.readValue(fileIson, CommonFile.class);
        String flag = FileUtil.download(commonFile.getFilePath(),
                commonFile.getFileName());
        if ("200".equals(flag)) {
            restultContent.setStatus(200);
        } else {
            restultContent.setStatus(300);
            restultContent.setErrorMsg("下载失败");
        }
        return restultContent;

    }

    @RequestMapping("/downLoad")
    @ResponseBody
    public void fileDownLoad(HttpServletResponse httpServletResponse,
                             @RequestBody(required = false) String fileIson) throws Exception {
        System.out.println("===============开始下载");
        System.out.println("===============下载参数为" + fileIson);
        RestultContent restultContent = new RestultContent();
        CommonFile commonFile = JSONUtil.readValue(fileIson, CommonFile.class);
        File file = new File("D:\\titan-file\\static\\20190724105857_beb9846f-937c-4107-ad3c-bedded9c5d08\\hgtitan-security.sql");

        if (!file.exists()) {
            httpServletResponse.getWriter().print("文件丢失..");
            httpServletResponse.getWriter().flush();
            httpServletResponse.getWriter().close();
            return;
        }

        httpServletResponse.setContentType("application/force-download");// 设置强制下载不打开
        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName()));// 设置文件名

        byte[] buffer = new byte[4096];// 缓冲区
        BufferedOutputStream output = null;
        BufferedInputStream input = null;
        try {
            output = new BufferedOutputStream(httpServletResponse.getOutputStream());
            input = new BufferedInputStream(new FileInputStream(file));
            int n = -1;
            // 遍历，开始下载
            while ((n = input.read(buffer)) > -1) {
                output.write(buffer, 0, n);
            }
            output.flush(); // 不可少
            httpServletResponse.flushBuffer();// 不可少
        } catch (Exception e) {
            // 异常自己捕捉
            throw e;
        } finally {
            // 关闭流，不可少
            if (input != null)
                input.close();
            if (output != null)
                output.close();
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
