package org.vhorvath.valogato.common.dao.lowlevel.configuration.general;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.vhorvath.valogato.common.beans.configuration.general.CacheBean;
import org.vhorvath.valogato.common.beans.configuration.general.GeneralConfigurationBean;
import org.vhorvath.valogato.common.beans.configuration.general.NewFeatureBean;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({GeneralConfigurationUtils.class})
public class GeneralConfigurationUtilsTest {


	@Before
	public void setUp() throws Exception {
		PowerMock.mockStaticPartial(GeneralConfigurationUtils.class, "init");
		Whitebox.invokeMethod(GeneralConfigurationUtils.class, "init");
		PowerMock.expectLastCall();
		PowerMock.replay(GeneralConfigurationUtils.class);

		GeneralConfigurationBean generalConfiguration = new GeneralConfigurationBean();
		String src = 	"<generalConfiguration>"+
						"	<newFeature name='somefeature' clazz='org.vhorvath.valogato.controller.features.SomeFeature'>"+
						"		<param name='someParam' title='Some Parameter' />"+
						"		<param name='anyParam' title='Any Parameter' />"+
						"	</newFeature>"+
						"	<newFeature name='TheLatest' clazz='org.nasa.hyper.TheLatest'/>"+
						"	<backendserviceConfigSource>CACHE</backendserviceConfigSource>"+
						"	<statisticsSource>CACHE</statisticsSource>"+
						"	<cache type='Terracotta'>"+
						"		<param name='distributedCacheName'>THROTTLING_DISTRIBUTED_STORE</param>"+
						"	</cache>"+
						"</generalConfiguration>";
		Serializer serializer = new Persister();
		generalConfiguration = serializer.read(GeneralConfigurationBean.class, src);
		GeneralConfigurationUtils.setGeneralConfiguration(generalConfiguration);
	}
	
	
//	@Test
//	public void testGetFeature() throws Exception {
//		String nameFeature = "TheLatest";
//		
//		NewFeatureBean newFeature = GeneralConfigurationUtils.getFeature(nameFeature );
//		
//		assertEquals("org.nasa.hyper.TheLatest", newFeature.getClazz());
//	}
//
//
//	@Test
//	public void testGetFeature_notFound() throws Exception {
//		String nameFeature = "unknown";
//		
//		try {
//			GeneralConfigurationUtils.getFeature(nameFeature);
//			fail("ThrottlingConfigurationException should have been thrown!");
//		} catch(ThrottlingConfigurationException e) {
//			assertEquals(String.format("The feature '%s' cannot be found in the general configuration XML file!",nameFeature), e.getMessage());
//		}
//	}

	
	@Test
	public void testGetFeatures() throws ThrottlingConfigurationException {
		List<NewFeatureBean> newFeatures = GeneralConfigurationUtils.getFeatures();
		
		assertEquals(2, newFeatures.size());
	}
	
	
	@Test
	public void testGetBackendserviceConfigSource() throws ThrottlingConfigurationException {
		String source = GeneralConfigurationUtils.getBackendserviceConfigSource();
		
		assertEquals("CACHE", source);
	}


	@Test
	public void testGetBackendserviceConfigSource_oneSpace() throws Exception {
		GeneralConfigurationBean generalConfiguration = new GeneralConfigurationBean();
		String src = 	"<generalConfiguration>"+
						"	<newFeature name='somefeature' clazz='org.vhorvath.valogato.controller.features.SomeFeature'>"+
						"		<param name='someParam' title='Some Parameter' />"+
						"		<param name='anyParam' title='Any Parameter' />"+
						"	</newFeature>"+
						"	<newFeature name='TheLatest' clazz='org.nasa.hyper.TheLatest'/>"+
						"	<backendserviceConfigSource> </backendserviceConfigSource>"+
						"	<statisticsSource>CACHE</statisticsSource>"+
						"	<cache type='Terracotta'>"+
						"		<param name='distributedCacheName'>THROTTLING_DISTRIBUTED_STORE</param>"+
						"	</cache>"+
						"</generalConfiguration>";
		Serializer serializer = new Persister();
		generalConfiguration = serializer.read(GeneralConfigurationBean.class, src);
		GeneralConfigurationUtils.setGeneralConfiguration(generalConfiguration);

		try {
			GeneralConfigurationUtils.getBackendserviceConfigSource();
			fail("ThrottlingConfigurationException should have been thrown!");
		} catch(ThrottlingConfigurationException e) {
			assertEquals(String.format("The source of the backendservice config file (e.g. <backendserviceConfigSource>" +
					"DUMMY</backendserviceConfigSource>) contains an invalid value! invalid value:'%s'", " "), e.getMessage());
		}
	}


	@Test
	public void testGetBackendserviceConfigSource_invalidValue() throws Exception {
		GeneralConfigurationBean generalConfiguration = new GeneralConfigurationBean();
		String src = 	"<generalConfiguration>"+
						"	<newFeature name='somefeature' clazz='org.vhorvath.valogato.controller.features.SomeFeature'>"+
						"		<param name='someParam' title='Some Parameter' />"+
						"		<param name='anyParam' title='Any Parameter' />"+
						"	</newFeature>"+
						"	<newFeature name='TheLatest' clazz='org.nasa.hyper.TheLatest'/>"+
						"	<backendserviceConfigSource>nemjo</backendserviceConfigSource>"+
						"	<statisticsSource>CACHE</statisticsSource>"+
						"	<cache type='Terracotta'>"+
						"		<param name='distributedCacheName'>THROTTLING_DISTRIBUTED_STORE</param>"+
						"	</cache>"+
						"</generalConfiguration>";
		Serializer serializer = new Persister();
		generalConfiguration = serializer.read(GeneralConfigurationBean.class, src);
		GeneralConfigurationUtils.setGeneralConfiguration(generalConfiguration);

		try {
			GeneralConfigurationUtils.getBackendserviceConfigSource();
			fail("ThrottlingConfigurationException should have been thrown!");
		} catch(ThrottlingConfigurationException e) {
			assertEquals(String.format("The source of the backendservice config file (e.g. <backendserviceConfigSource>" +
					"DUMMY</backendserviceConfigSource>) contains an invalid value! invalid value:'%s'", "nemjo"), e.getMessage());
		}
	}


	@Test
	public void testGetStatisticsSource() throws ThrottlingConfigurationException {
		String source = GeneralConfigurationUtils.getStatisticsSource();
		
		assertEquals("CACHE", source);
	}


	@Test
	public void testGetStatisticsSource_oneSpace() throws Exception {
		GeneralConfigurationBean generalConfiguration = new GeneralConfigurationBean();
		String src = 	"<generalConfiguration>"+
						"	<newFeature name='somefeature' clazz='org.vhorvath.valogato.controller.features.SomeFeature'>"+
						"		<param name='someParam' title='Some Parameter' />"+
						"		<param name='anyParam' title='Any Parameter' />"+
						"	</newFeature>"+
						"	<newFeature name='TheLatest' clazz='org.nasa.hyper.TheLatest'/>"+
						"	<backendserviceConfigSource>CACHE</backendserviceConfigSource>"+
						"	<statisticsSource> </statisticsSource>"+
						"	<cache type='Terracotta'>"+
						"		<param name='distributedCacheName'>THROTTLING_DISTRIBUTED_STORE</param>"+
						"	</cache>"+
						"</generalConfiguration>";
		Serializer serializer = new Persister();
		generalConfiguration = serializer.read(GeneralConfigurationBean.class, src);
		GeneralConfigurationUtils.setGeneralConfiguration(generalConfiguration);

		try {
			GeneralConfigurationUtils.getStatisticsSource();
			fail("ThrottlingConfigurationException should have been thrown!");
		} catch(ThrottlingConfigurationException e) {
			assertEquals(String.format("The source of the statisticsSource (e.g. <statisticsSource>DUMMY" +
					"</statisticsSource>) contains an invalid value! invalid value:'%s'", " "), e.getMessage());
		}
	}


	@Test
	public void testGetStatisticsSource_invalidValue() throws Exception {
		GeneralConfigurationBean generalConfiguration = new GeneralConfigurationBean();
		String src = 	"<generalConfiguration>"+
						"	<newFeature name='somefeature' clazz='org.vhorvath.valogato.controller.features.SomeFeature'>"+
						"		<param name='someParam' title='Some Parameter' />"+
						"		<param name='anyParam' title='Any Parameter' />"+
						"	</newFeature>"+
						"	<newFeature name='TheLatest' clazz='org.nasa.hyper.TheLatest'/>"+
						"	<backendserviceConfigSource>CACHE</backendserviceConfigSource>"+
						"	<statisticsSource>nemjo</statisticsSource>"+
						"	<cache type='Terracotta'>"+
						"		<param name='distributedCacheName'>THROTTLING_DISTRIBUTED_STORE</param>"+
						"	</cache>"+
						"</generalConfiguration>";
		Serializer serializer = new Persister();
		generalConfiguration = serializer.read(GeneralConfigurationBean.class, src);
		GeneralConfigurationUtils.setGeneralConfiguration(generalConfiguration);

		try {
			GeneralConfigurationUtils.getStatisticsSource();
			fail("ThrottlingConfigurationException should have been thrown!");
		} catch(ThrottlingConfigurationException e) {
			assertEquals(String.format("The source of the statisticsSource (e.g. <statisticsSource>DUMMY" +
					"</statisticsSource>) contains an invalid value! invalid value:'%s'", "nemjo"), e.getMessage());
		}
	}

	
	@Test
	public void testGetCache() throws ThrottlingConfigurationException {
		CacheBean cacheBean = GeneralConfigurationUtils.getCache();
		
		assertEquals("Terracotta", cacheBean.getType());
	}
}