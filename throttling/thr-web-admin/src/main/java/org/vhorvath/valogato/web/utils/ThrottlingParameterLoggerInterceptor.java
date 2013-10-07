package org.vhorvath.valogato.web.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.constants.ThrConstants;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class ThrottlingParameterLoggerInterceptor implements Interceptor {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_WEB_NAME);

	@Override
	public void destroy() {
	}

	@Override
	public void init() {
	}

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		StringBuffer logBuffer = new StringBuffer();
		logBuffer.append("\n##########################################################################\n");
		logBuffer.append("Action: ").append(invocation.getAction().getClass().getName()).append("\n");
		logBuffer.append("Invocation Context Name: ").append(invocation.getInvocationContext().getName()).append("\n");
		logBuffer.append("Parameters: [");
		if (invocation.getInvocationContext().getParameters() != null && invocation.getInvocationContext().getParameters().keySet().size() > 0) {
			logBuffer.append("\n");
			for(String key : invocation.getInvocationContext().getParameters().keySet()) {
				Object value = invocation.getInvocationContext().getParameters().get(key);
				logBuffer.append("     ").append(key).append(":");
				if (value instanceof Object[] && ((Object[])value).length > 1) {
					for(Object obj : (Object[])value) {
						logBuffer.append("\n          ").append(obj);
					}
				} else if (value instanceof Object[] && ((Object[])value).length > 0) {
					logBuffer.append(((Object[])value)[0]).append("\n");
				} else {
					logBuffer.append(value).append(" - ").append(value.getClass().getName()).append("\n");
				}
			}
		}
		logBuffer.append("]").append("\n");
		logBuffer.append("Proxy Method: ").append(invocation.getProxy().getMethod()).append("\n");

		
		String result = null;
		try {

			result = invocation.invoke();
			return result;
			
		} catch(Exception e) {
			LOGGER.error("Unexpected exception occurred!", e);
			throw e;
		}
		
		finally {

			logBuffer.append("Action values: [");
			for(Method method : invocation.getAction().getClass().getDeclaredMethods()) {
				if (method.getModifiers() == Modifier.PUBLIC && method.getName().startsWith("get")) {
					try {
						Object value = method.invoke(invocation.getAction());
						String valueString = value == null ? "null" : value.toString();
						logBuffer.append("\n     ").append(method.getName()).append(":").append(valueString);
					} catch(IllegalArgumentException e) {
						logBuffer.append("\n     ").append(method.getName()).append(":").append(e.getMessage());					
					}
				}
			}
			logBuffer.append("]").append("\n");
			logBuffer.append("Result: ").append(result).append("\n");
			logBuffer.append("##########################################################################");
			LOGGER.debug(logBuffer.toString());
		}
		
	}
	
}
