package config.webapp.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.riozenc.titanTool.common.json.utils.JSONUtil;
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
	public Map<String,List<CommonParamDomain>> dropMap=new HashMap<String,List<CommonParamDomain>>();
    public Map<String,String> ListValueMap=new HashMap<String, String>();
	@Autowired
	@Qualifier("commonParamServiceImpl")
	private ICommonParamService commonParamService;

	@PostMapping("getSysCommConfig")
	@ResponseBody
	public Object getCommonParam(@RequestBody CommonParamDomain commonParamDomain) {
		List<CommonParamDomain> list = commonParamService.findByWhere(commonParamDomain);
		return list;
	}

	@PostMapping("getAllSysCommConfig")
	@ResponseBody
	public Object getAllSysCommConfig(@RequestBody String a) {

        CommonParamDomain domain=new CommonParamDomain();
        List<CommonParamDomain> typeList=commonParamService.getAllType(domain);
        for (CommonParamDomain dom:typeList) {
            domain.setType(dom.getType());
            List<CommonParamDomain> list = commonParamService.findByWhere(domain);
            dropMap.put(dom.getType(),list);
        }

        return dropMap;
	}

    @PostMapping("getAllSysCommConfigForList")
    @ResponseBody
    public Object getAllSysCommConfigForList(@RequestBody String a) {
try {
    List<CommonParamDomain> list = commonParamService.getAllTypeForList(a);
    for (CommonParamDomain domain : list) {
        ListValueMap.put(domain.getType(), domain.getParamValue());
    }
}catch (Exception e ){
    e.printStackTrace();
}
        return ListValueMap;
    }
}
