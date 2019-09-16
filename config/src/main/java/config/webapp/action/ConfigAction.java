package config.webapp.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.riozenc.titanTool.common.json.utils.GsonUtils;
import com.riozenc.titanTool.spring.web.http.HttpResult;

import config.webapp.domain.CommonParamDomain;
import config.webapp.service.ICommonParamService;

@ControllerAdvice
@RequestMapping("sysCommConfig")
public class ConfigAction {

	@Autowired
	@Qualifier("commonParamServiceImpl")
	private ICommonParamService commonParamService;

	@PostMapping("test")
	public void test(@RequestBody String a) {
		synchronized (a) {
			System.out.println(a);
		}
	}

	@PostMapping("getSysCommConfig")
	@ResponseBody
	public Object getCommonParam(@RequestBody CommonParamDomain commonParamDomain) {
		List<CommonParamDomain> list = commonParamService.findByWhere(commonParamDomain);
		return list;
	}

	@PostMapping("findByWhere")
	@ResponseBody
	public Object findByWhere(@RequestBody CommonParamDomain commonParamDomain) {
		List<CommonParamDomain> typeList = commonParamService.findByWhere(commonParamDomain);
		return typeList;
	}

	@PostMapping("getAllSysCommConfig")
	@ResponseBody
	public Object getAllSysCommConfig(@RequestBody String a) {
		Map<String, List<CommonParamDomain>> dropMap = new HashMap<String, List<CommonParamDomain>>();
		CommonParamDomain domain = new CommonParamDomain();
		List<CommonParamDomain> typeList = commonParamService.getAllType(a);
		for (CommonParamDomain dom : typeList) {
			domain.setType(dom.getType());
			List<CommonParamDomain> list = commonParamService.findByWhere(domain);
			dropMap.put(dom.getType(), list);
		}
		return dropMap;
	}

	@PostMapping("getAllSysCommConfigForList")
	@ResponseBody
	public Object getAllSysCommConfigForList(@RequestBody String a) {
		Map<String, String> ListValueMap = new HashMap<String, String>();
		try {
			List<CommonParamDomain> list = commonParamService.getAllTypeForList(a);
			for (CommonParamDomain domain : list) {
				ListValueMap.put(domain.getType(), domain.getParamValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ListValueMap;
	}

	@PostMapping("getCurrentMon")
	@ResponseBody
	public Object getCurrentMon() throws Exception {
		return commonParamService.getCurrentMon();
	}

	@PostMapping("update")
	@ResponseBody
	public HttpResult update(@RequestBody CommonParamDomain c) {

//		CommonParamDomain commonParamDomain = new CommonParamDomain();
//		commonParamDomain.setType(c.getType());
//		commonParamDomain.setParamKey(c.getParamKey());
//		List<CommonParamDomain> commonParamDomains = commonParamService.findByWhere(commonParamDomain);

//		if (commonParamDomains != null && commonParamDomains.size() > 0) {
//			return new HttpResult(HttpResult.ERROR, "更新异常:已存相同的下拉标识与下拉值");
//		}
		try {
			commonParamService.update(c);
		} catch (Exception e) {
			e.printStackTrace();
			return new HttpResult(HttpResult.ERROR, "更新失败");
		}
		return new HttpResult(HttpResult.SUCCESS, "成功");
	}

	// 获取下拉备注
	@PostMapping("getDistinctRemark")
	@ResponseBody
	public List<String> getDistinctRemark() {
		return commonParamService.getDistinctRemark();
	}

	private HttpResult insert(CommonParamDomain c) {

		CommonParamDomain commonParamDomain = new CommonParamDomain();
		commonParamDomain.setType(c.getType());
		commonParamDomain.setParamKey(c.getParamKey());
		List<CommonParamDomain> commonParamDomains = commonParamService.findByWhere(commonParamDomain);
		if (commonParamDomains != null && commonParamDomains.size() > 0) {
			return new HttpResult(HttpResult.ERROR, "新增异常:已存相同的下拉标识与下拉值");
		}
		try {
			commonParamService.insert(c);
		} catch (Exception e) {
			e.printStackTrace();
			return new HttpResult(HttpResult.ERROR, "新增失败");
		}
		return new HttpResult(HttpResult.SUCCESS, "成功");
	}

	// 获取下拉备注
	@PostMapping("updateCommonParam")
	@ResponseBody
	public HttpResult updateCommonParam(@RequestBody String insertJson) throws IOException {
		CommonParamDomain commonParamDomain = GsonUtils.readValue(insertJson, CommonParamDomain.class);
		// 更新
		if (commonParamDomain.getId() == null) {
			return insert(commonParamDomain);
		} else {
			return update(commonParamDomain);
		}
	}

}
