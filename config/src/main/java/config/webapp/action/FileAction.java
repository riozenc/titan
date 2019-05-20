/**
 * Author : czy
 * Date : 2019年5月17日 下午9:22:14
 * Title : config.webapp.action.FileAction.java
 **/
package config.webapp.action;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import config.webapp.domain.RestultContent;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
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

        RestultContent restultContent = new RestultContent();
        Map<String, String> returnMap = new HashMap<>();
        if (!file.isEmpty()) {
            // 构建上传文件的存放路径
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String dateFolder = sdf.format(new Date());
            //绝对路径
            String absolutePath = Global.getConfig("project.path") + File.separator +
                    Global.getConfig("file.doc.path") + File.separator + dateFolder;
            //相对路径
            String relativePath = File.separator + Global.getConfig("file.doc.path") + File.separator + dateFolder;
            // 获取上传的文件名称，并结合存放路径，构建新的文件名称
            String fileName = file.getOriginalFilename();
            String saveName = getFileNameNew();
            String fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            //地址 D:/titan-file/titan-file/20190518/自定义名
            File filepath = new File(absolutePath, saveName);
            // 判断路径是否存在，不存在则新创建一个
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();
            }
            file.transferTo(new File(absolutePath + File.separator + saveName + fileType));
            returnMap.put("absolutePath", absolutePath + File.separator + saveName + fileType);
            returnMap.put("relativePath", relativePath + File.separator + saveName + fileType);
            returnMap.put("fileName", fileName);
            restultContent.setStatus(200);
            restultContent.setData(returnMap);
        } else {
            restultContent.setStatus(500);
            restultContent.setErrorMsg("文件不存在");
        }
        return restultContent;
    }

    /**
     * 重新命名文件
     * 毫秒数+随机数
     *
     * @return
     */
    public String getFileNameNew() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        return fmt.format(new Date()) + "_" + UUID.randomUUID().toString();
    }
}
