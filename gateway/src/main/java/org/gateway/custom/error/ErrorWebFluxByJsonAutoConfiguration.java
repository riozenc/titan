/**
 *    Auth:riozenc
 *    Date:2019年3月4日 上午10:53:11
 *    Title:org.gateway.custom.error.ErrorWebFluxByJsonAutoConfiguration.java
 **/
package org.gateway.custom.error;

import java.util.Collections;
import java.util.List;

import org.gateway.exception.handler.JsonExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(WebFluxConfigurer.class)
@AutoConfigureBefore(ErrorWebFluxAutoConfiguration.class)
@EnableConfigurationProperties({ ServerProperties.class, ResourceProperties.class })
public class ErrorWebFluxByJsonAutoConfiguration {

	private final ServerProperties serverProperties;

	private final ApplicationContext applicationContext;

	private final ResourceProperties resourceProperties;

	private final List<ViewResolver> viewResolvers;

	private final ServerCodecConfigurer serverCodecConfigurer;

	public ErrorWebFluxByJsonAutoConfiguration(ServerProperties serverProperties, ResourceProperties resourceProperties,
			ObjectProvider<List<ViewResolver>> viewResolversProvider, ServerCodecConfigurer serverCodecConfigurer,
			ApplicationContext applicationContext) {
		this.serverProperties = serverProperties;
		this.applicationContext = applicationContext;
		this.resourceProperties = resourceProperties;
		this.viewResolvers = viewResolversProvider.getIfAvailable(() -> Collections.emptyList());
		this.serverCodecConfigurer = serverCodecConfigurer;
	}

	@Bean
	@ConditionalOnMissingBean(value = ErrorWebExceptionHandler.class, search = SearchStrategy.CURRENT)
	@Order(-1)
	public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes) {
		JsonExceptionHandler exceptionHandler = new JsonExceptionHandler(errorAttributes, this.resourceProperties,
				this.serverProperties.getError(), this.applicationContext);
		exceptionHandler.setViewResolvers(this.viewResolvers);
		exceptionHandler.setMessageWriters(this.serverCodecConfigurer.getWriters());
		exceptionHandler.setMessageReaders(this.serverCodecConfigurer.getReaders());
		return exceptionHandler;
	}

	@Bean
	@ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
	public JsonErrorAttributes errorAttributes() {
		return new JsonErrorAttributes(this.serverProperties.getError().isIncludeException());
	}

}
