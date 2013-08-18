package org.vhorvath.valogato.web.actions;


import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;

import com.opensymphony.xwork2.ActionSupport;


public class ThrottlingActionSupport extends ActionSupport {

	
	protected static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_WEB_NAME);
	
	
	protected void checkInteger(ThrottlingActionSupport action, String value, String field, String fieldLabel, int... ranges) {
		checkRequired(action, value, field, fieldLabel);
		if (action.getFieldErrors().isEmpty()) {
			try {
				Integer i = Integer.parseInt(value);
				if (ranges.length == 2) {
					if (i < ranges[0] || i > ranges[1]) {
						action.addFieldError(field, String.format("%s must be in the range %s and %s.", fieldLabel,
								Integer.toString(ranges[0]), Integer.toString(ranges[1])));
					}
				}
			} catch(NumberFormatException nfe) {
				action.addFieldError(field, String.format("%s must be an integer.",fieldLabel));
			}
		}
	}
	
	protected void checkRequired(ThrottlingActionSupport action, String value, String field, String fieldLabel) {
		if (value.length() == 0) {
			action.addFieldError(field, String.format("%s is required.",fieldLabel));
		}
	}

	protected void checkPossibleValues(ThrottlingActionSupport action, String value, String field, String fieldLabel, String... values) {
		checkRequired(action, value, field, fieldLabel);
		if (action.getFieldErrors().isEmpty()) {
			if (Arrays.binarySearch(values, value) < 0) {
				action.addFieldError(field, String.format("The value (%s) of %s must be in the set (%s).", value, fieldLabel, ThrottlingUtils.commaSeparated(values)));
			}
		}
	}
	
}
