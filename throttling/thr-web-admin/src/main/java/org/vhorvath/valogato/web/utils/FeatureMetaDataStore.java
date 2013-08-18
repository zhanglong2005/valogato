package org.vhorvath.valogato.web.utils;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vhorvath.valogato.common.beans.configuration.general.NewFeatureParamBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

public final class FeatureMetaDataStore {

	private FeatureMetaDataStore() { }
	
	public static List<NewFeatureParamBean> getParams(String nameFeature) throws ThrottlingConfigurationException {
		checkArgument(nameFeature != null);
		
		if (nameFeature.equals(ThrConstants.Features.SendBackFaultFeature.name())) {
			return Collections.emptyList();
		} else if (nameFeature.equals(ThrConstants.Features.WaitingFeature.name())) {
			List<NewFeatureParamBean> params = new ArrayList<NewFeatureParamBean>();
			params.add(new NewFeatureParamBean(ThrConstants.FeatureParam.period.getName(), ThrConstants.FeatureParam.period.getTitle()));
			params.add(new NewFeatureParamBean(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.getName(), 
					ThrConstants.FeatureParam.maxNumberOfWaitingReqs.getTitle()));
			params.add(new NewFeatureParamBean(ThrConstants.FeatureParam.waitingReqListMaxSize.getName(), 
					ThrConstants.FeatureParam.waitingReqListMaxSize.getTitle()));
			params.add(new NewFeatureParamBean(ThrConstants.FeatureParam.strategy.getName(), 
					ThrConstants.FeatureParam.strategy.getTitle()));
			return params;

		} else if (nameFeature.equals(ThrConstants.Features.ForwarderFeature.name())) {
			List<NewFeatureParamBean> params = new ArrayList<NewFeatureParamBean>();
			params.add(new NewFeatureParamBean(ThrConstants.FeatureParam.endpoints.getName(), ThrConstants.FeatureParam.endpoints.getTitle()));
			return params;
		} 
			
		
		else {
//			NewFeatureBean newFeature = GeneralConfigurationUtils.getFeature(nameFeature);
//			if (newFeature == null) {
			throw new ThrottlingConfigurationException(String.format("The %s is not a valid feature name!", nameFeature));
//			}
//			return newFeature.getParam();
		}
	}
	
	public static List<String> getFeatures() throws ThrottlingConfigurationException {
		List<String> features = new ArrayList<String>();
		features.add(ThrConstants.Features.ForwarderFeature.name());
		features.add(ThrConstants.Features.SendBackFaultFeature.name());
		features.add(ThrConstants.Features.WaitingFeature.name());
//		for(NewFeatureBean newFeatureBean : GeneralConfigurationUtils.getFeatures()) {
//			features.add(newFeatureBean.getName());
//		}
		return features;
	}
	
}
