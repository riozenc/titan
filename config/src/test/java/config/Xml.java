package config;

import java.io.IOException;

import com.riozenc.titanTool.common.ClassDAOXmlUtil;

import config.webapp.domain.CommonParamDomain;

public class Xml {
public static void main(String[] args) {
	try {
		ClassDAOXmlUtil.buildXML("C:\\Users\\czy\\git\\titan\\config\\src\\main\\java\\config\\webapp\\dao",
				CommonParamDomain.class, "SYSTEM_COMMON_CONFIG");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
