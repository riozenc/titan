/**
 * Author : chizf
 * Date : 2020年3月30日 下午3:41:23
 * Title : org.rs.event.GateWayRegisteredEntity.java
 *
**/
package org.gateway.handler.entity;

public class RegistrationGatewayEntity {
	private String id;
	private int order;
	private String uri;
	private String filters;

	private String appName;
	/**
	 * [{ "args": { "_genkey_0": "/demoApi/**" }, "name": "Path" }]
	 */
	private String predicates;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public String getPredicates() {
		return predicates;
	}

	public void setPredicates(String predicates) {
		this.predicates = predicates;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

}
