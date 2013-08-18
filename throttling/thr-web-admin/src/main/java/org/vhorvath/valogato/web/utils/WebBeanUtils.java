package org.vhorvath.valogato.web.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.SimulatedServiceBean;
import org.vhorvath.valogato.common.beans.configuration.general.NewFeatureParamBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.web.beans.backendservice.FeatureParamWebBean;
import org.vhorvath.valogato.web.beans.backendservice.FeatureWebBean;
import org.vhorvath.valogato.web.beans.backendservice.SimulatedServiceWebBean;


public final class WebBeanUtils {

	private WebBeanUtils() { }
	
	
	public static String getCommaSeparatedFeatures(BackendServiceBean backendService) {
		StringBuffer result = new StringBuffer();
		result.append(backendService.getFeature().getName()).append(",[");
		if (backendService.getSimulatedService() != null) {
			Set<String> allFeatures = new TreeSet<String>();
			for(SimulatedServiceBean simulatedService : backendService.getSimulatedService()) {
				allFeatures.add(simulatedService.getFeature().getName());
			}
			for (String f : allFeatures.toArray(new String[0])) {
				result.append(f).append(",");
			}
		}
		result.append("]");
		return result.toString().replaceAll(",]", "]");
	}
	
	
	public static String getCommaSeparatedSimServices(BackendServiceBean backendService) {
		StringBuffer result = new StringBuffer();
		if (backendService.getSimulatedService() != null) {
			Set<String> allSimService = new TreeSet<String>();
			for(SimulatedServiceBean simulatedService : backendService.getSimulatedService()) {
				allSimService.add(simulatedService.getName());
			}
			for (String ss : allSimService.toArray(new String[0])) {
				result.append(ss).append(",");
			}
		}
		return result.length() == 0 ? result.toString() : result.substring(0, result.length()-1).toString();
	}

	
	public static List<FeatureWebBean> loadFeatures() throws ThrottlingConfigurationException {
		List<FeatureWebBean> result = new ArrayList<FeatureWebBean>();
		result.add(new FeatureWebBean(ThrConstants.Features.ForwarderFeature.name(),
                                      ThrConstants.Features.ForwarderFeature.name() + " [Builtin]"));
		result.add(new FeatureWebBean(ThrConstants.Features.SendBackFaultFeature.name(),
				                      ThrConstants.Features.SendBackFaultFeature.name() + " [Builtin]"));
		result.add(new FeatureWebBean(ThrConstants.Features.WaitingFeature.name(), 
				                      ThrConstants.Features.WaitingFeature.name() + " [Builtin]"));

//		List<NewFeatureBean> listNewFeatureBean = GeneralConfigurationUtils.getFeatures();
//		for (NewFeatureBean featureBean : listNewFeatureBean) {
//			result.add(new FeatureWebBean(featureBean.getName(), featureBean.getName() + " [New]"));
//		}

		Collections.sort(result, new FeatureWebBeanComparator());

		return result;
	}

	
	public static List<SimulatedServiceWebBean> getSimulatedServiceList(BackendServiceBean backendServiceBean) {
		List<SimulatedServiceWebBean> simulatedServiceWebBeanList = new ArrayList<SimulatedServiceWebBean>();
		if (backendServiceBean.getSimulatedService() != null) {
			for(SimulatedServiceBean simulatedServiceBean : backendServiceBean.getSimulatedService()) {
				simulatedServiceWebBeanList.add(new SimulatedServiceWebBean(simulatedServiceBean.getName(), 
						                                                    simulatedServiceBean.getFeature().getName(), 
						                                                    backendServiceBean.getName()));
			}
		}
		return simulatedServiceWebBeanList;
	}


	public static List<FeatureParamWebBean> getFeatureParams(String selectedFeature,  Map<String, String> featureParams) 
			throws ThrottlingConfigurationException {
		List<FeatureParamWebBean> featureParamWebBeans = new ArrayList<FeatureParamWebBean>();
		List<NewFeatureParamBean> newFeatureParamBeans = FeatureMetaDataStore.getParams(selectedFeature);
		if (newFeatureParamBeans != null) {
			for(NewFeatureParamBean newFeatureParamBean : newFeatureParamBeans) {
				String value = featureParams == null ? null : featureParams.get(newFeatureParamBean.getName());
				try { value = value == null ? ThrConstants.FeatureParam.valueOf(newFeatureParamBean.getName()).getDefault() : value;
				} catch(IllegalArgumentException e) {}
				
				featureParamWebBeans.add(new FeatureParamWebBean(newFeatureParamBean.getName(), value, newFeatureParamBean.getTitle()));
			}
		}
		return featureParamWebBeans;
	}


	public static FeatureBean convertFeatureWebBeanToBean(String selectedFeature, List<FeatureParamWebBean> featureParamWebBeans) {
		FeatureBean featureBean = new FeatureBean();
		featureBean.setName(selectedFeature);
		if (featureParamWebBeans != null) {
			if (featureParamWebBeans.size() > 0) {
				featureBean.setParams(new HashMap<String, String>());
			}
			for(FeatureParamWebBean featureParamWebBean : featureParamWebBeans) {
				featureBean.getParams().put(featureParamWebBean.getName(), featureParamWebBean.getValue());
			}
		}
		return featureBean;
	}


}


class FeatureWebBeanComparator implements Comparator<FeatureWebBean> {

	@Override
	public int compare(FeatureWebBean o1, FeatureWebBean o2) {
		return o1.getKey().compareToIgnoreCase(o2.getKey());
	}

}