/**
 *    Auth:riozenc
 *    Date:2018年5月10日 上午11:03:27
 *    Title:cis.support.mybatis.PaginationInterceptor.java
 **/
package config.support.mybatis;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.riozenc.titanTool.annotation.PaginationSupport;

@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
		RowBounds.class, ResultHandler.class }) })
public class PaginationInterceptor extends com.riozenc.titanTool.mybatis.pagination.interceptor.PaginationInterceptor {

	private static final long serialVersionUID = 85927236125747430L;

	private static Class<?>[] params = new Class[] {};

	@Override
	protected boolean preHandle(MappedStatement mappedStatement, Object parameter) {
		// TODO Auto-generated method stub

		String name = mappedStatement.getId();

		String className = name.substring(0, name.lastIndexOf("."));
		String methodName = name.substring(name.lastIndexOf(".") + 1);

		try {
			Class<?> clazz = Class.forName(className);

			Method method = null;
			if (parameter == null) {
				method = clazz.getDeclaredMethod(methodName, params);
			} else {
				method = clazz.getDeclaredMethod(methodName, parameter.getClass());
			}

			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				if (PaginationSupport.class == annotation.annotationType()) {
					return true;
				}
			}

		} catch (Exception exception) {
			return false;
		}

		return false;
	}

	@Override
	protected void postHandle(MappedStatement mappedStatement, Object parameter) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void afterCompletion(MappedStatement mappedStatement, Object parameter, Exception exception) {
		// TODO Auto-generated method stub

	}

}
