package config.webapp.service.impl;

import java.util.List;

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

	@Override
	public String getCurrentMon() throws Exception {
		// TODO Auto-generated method stub

		CommonParamDomain commonParamDomain = new CommonParamDomain();

		commonParamDomain.setType("CURRENT_MON");
		commonParamDomain.setParamKey(0);
		commonParamDomain.setStatus((byte)1);

		List<CommonParamDomain> list = commonParamDAO.findByWhere(commonParamDomain);
		if (list.isEmpty()) {
			throw new Exception("未找到月份数据.");
		}
		return list.get(0).getParamValue();
	}

	@Override
	public List<String> getDistinctRemark(){
		return commonParamDAO.getDistinctRemark();
	}
}
