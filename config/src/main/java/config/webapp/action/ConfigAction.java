package config.webapp.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import config.webapp.domain.CommonParamDomain;
import config.webapp.service.ICommonParamService;

@ControllerAdvice
@RequestMapping("sysCommConfig")
public class ConfigAction {

	@Autowired
	@Qualifier("commonParamServiceImpl")
	private ICommonParamService commonParamService;

	@PostMapping("getSysCommConfig")
	@ResponseBody
	public Object getCommonParam(@RequestBody CommonParamDomain commonParamDomain) {
		List<CommonParamDomain> list = commonParamService.findByWhere(commonParamDomain);
		return list;
	}
}
