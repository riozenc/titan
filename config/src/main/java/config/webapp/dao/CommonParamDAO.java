package config.webapp.dao;

import java.util.List;

import com.riozenc.titanTool.annotation.TransactionDAO;
import com.riozenc.titanTool.spring.webapp.dao.AbstractTransactionDAOSupport;
import com.riozenc.titanTool.spring.webapp.dao.BaseDAO;

import config.webapp.domain.CommonParamDomain;

@TransactionDAO
public class CommonParamDAO extends AbstractTransactionDAOSupport implements BaseDAO<CommonParamDomain> {

	@Override
	public int insert(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return getPersistanceManager().insert(getNamespace() + ".insert", t);
	}

	@Override
	public int delete(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return getPersistanceManager().delete(getNamespace() + ".delete", t);
	}

	@Override
	public int update(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return getPersistanceManager().update(getNamespace() + ".update", t);

	}

	@Override
	public CommonParamDomain findByKey(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return getPersistanceManager().load(getNamespace() + ".findByKey", t);
	}

	@Override
	public List<CommonParamDomain> findByWhere(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return getPersistanceManager().find(getNamespace() + ".findByWhere", t);
	}

	public List<CommonParamDomain> getAllType(CommonParamDomain t) {
		// TODO Auto-generated method stub
		return getPersistanceManager().find(getNamespace() + ".getAllType", t);
	}

	public List<CommonParamDomain> getAllTypeForList(String t) {
		// TODO Auto-generated method stub
		return getPersistanceManager().find(getNamespace() + ".getAllTypeForList", t);
	}

}
