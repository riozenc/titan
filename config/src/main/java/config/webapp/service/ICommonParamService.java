package config.webapp.service;

import java.util.List;

import com.riozenc.titanTool.spring.webapp.service.BaseService;

import config.webapp.domain.CommonParamDomain;

public interface ICommonParamService extends BaseService<CommonParamDomain>{


    public List<CommonParamDomain> getAllType(String t);
    public List<CommonParamDomain> getAllTypeForList(String t);
    
    public String getCurrentMon() throws Exception;
}
