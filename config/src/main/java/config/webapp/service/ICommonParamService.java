package config.webapp.service;

import com.riozenc.titanTool.spring.webapp.service.BaseService;

import config.webapp.domain.CommonParamDomain;

import java.util.List;
import java.util.Map;

public interface ICommonParamService extends BaseService<CommonParamDomain>{


    public List<CommonParamDomain> getAllType(CommonParamDomain domain);
    public List<CommonParamDomain> getAllTypeForList(String t);
}
