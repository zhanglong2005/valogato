package org.vhorvath.valogato.common.dao.highlevel.usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.impl.DummyUsageDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.impl.MemoryUsageDAO;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;


@RunWith(PowerMockRunner.class)
@PrepareForTest({GeneralConfigurationUtils.class})
public class UsageDAOFactoryTest {

	
	@Before
	public void setUp() {
		ThrottlingStorage.setUsageDAOInstance(null);
	}
	
	@Test
	public void testGetDAO_cache() throws ThrottlingConfigurationException {
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getStatisticsSource()).andReturn(ThrConstants.Source.cache.toString());
		PowerMock.replay(GeneralConfigurationUtils.class);
		
		IUsageDAO object = UsageDAOFactory.getDAO();
		
		assertEquals(MemoryUsageDAO.class, object.getClass());
	}


	@Test
	public void testGetDAO_dummy() throws ThrottlingConfigurationException {
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getStatisticsSource()).andReturn(ThrConstants.Source.dummy.toString());
		PowerMock.replay(GeneralConfigurationUtils.class);
		
		IUsageDAO object = UsageDAOFactory.getDAO();
		
		assertEquals(DummyUsageDAO.class, object.getClass());
	}


	@Test
	public void testGetDAO_cache_unknown() throws ThrottlingConfigurationException {
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getStatisticsSource()).andReturn("unknown");
		PowerMock.replay(GeneralConfigurationUtils.class);
		
		try {
			UsageDAOFactory.getDAO();
			fail("ThrottlingConfigurationException should have been thrown!");
		} catch(ThrottlingConfigurationException e) {
			assertEquals("Uknown statisticsSource value in the configfuration! source=unknown", e.getMessage());			
		}
	}
	
}
