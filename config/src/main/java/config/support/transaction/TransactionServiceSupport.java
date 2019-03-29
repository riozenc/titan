/**
 *    Auth:riozenc
 *    Date:2018年5月10日 上午10:29:41
 *    Title:cis.web.support.transaction.TransactionServiceSupport.java
 **/
package config.support.transaction;

import org.springframework.context.annotation.Configuration;

import com.riozenc.titanTool.spring.transaction.registry.TransactionServiceRegistryPostProcessor;

@Configuration
public class TransactionServiceSupport extends TransactionServiceRegistryPostProcessor {

}
