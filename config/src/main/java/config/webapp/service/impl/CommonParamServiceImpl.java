package config.webapp.service.impl;

import java.util.List;
import java.util.Map;

import com.riozenc.titanTool.annotation.TransactionDAO;
import com.riozenc.titanTool.annotation.TransactionService;

import config.webapp.dao.CommonParamDAO;
import config.webapp.domain.CommonParamDomain;
import config.webapp.service.ICommonParamService;

@TransactionService
public class CommonParamServiceImpl implements ICommonParamService {
	@TransactionDAO("master")
	private CommonParamDAO commonParamDAO;

	@Override
	public int insert(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return commonParamDAO.insert(t);
	}

	@Override
	public int delete(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return commonParamDAO.delete(t);
	}

	@Override
	public int update(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return commonParamDAO.update(t);
	}

	@Override
	public CommonParamDomain findByKey(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return commonParamDAO.findByKey(t);
	}

	@Override
	public List<CommonParamDomain> findByWhere(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return commonParamDAO.findByWhere(t);
	}


	@Override
	public List<CommonParamDomain> getAllType(String t) {

		return commonParamDAO.getAllType(t);
	}

	@Override
	public List<CommonParamDomain> getAllTypeForList(String t) {

		return commonParamDAO.getAllTypeForList(t);
	}

}
