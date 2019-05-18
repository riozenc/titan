/**
 * Author : czy
 * Date : 2019年5月17日 下午9:22:14
 * Title : config.webapp.action.FileAction.java
 *
**/
package config.webapp.action;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.riozenc.titanTool.properties.Global;

@ControllerAdvice
@RequestMapping("file")
public class FileAction {

	public String upload(HttpServletRequest request, MultipartFile file) throws IllegalStateException, IOException {

		if (!file.isEmpty()) {
			// 构建上传文件的存放路径
			String path = Global.getConfig("project.path") + File.separator + Global.getConfig("file.doc.path");
			// 获取上传的文件名称，并结合存放路径，构建新的文件名称
			String filename = file.getOriginalFilename();
			File filepath = new File(path, filename);
			// 判断路径是否存在，不存在则新创建一个
			if (!filepath.exists()) {
				filepath.mkdirs();
			}
			file.transferTo(new File(path + File.separator + filename));
		}

		return null;
	}

}
