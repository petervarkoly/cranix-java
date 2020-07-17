package de.cranix.dao.controller;import java.util.ArrayList;

import javax.persistence.EntityManager;

import de.cranix.dao.Device;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.internal.CommonEntityManagerFactory;

public class RoomControllerTestCase extends OSSDaoTestCase  {
	public void testAddDevice() {
		Session s = getValidAdminSession();
		assertNotNull(s);
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();

		RoomController r = new RoomController(s ,em);
		
		ArrayList<Device> devices = new ArrayList<Device>();
		Device dev1 = new Device();
		dev1.setIp("10.0.0.16");
		dev1.setName("test6");
		dev1.setMac("11:22:33:44:55:26");
		dev1.setWlanMac("");
		dev1.setWlanIp("");
		devices.add(dev1);
		CrxResponse res = r.addDevices(1, devices);
		assertEquals("OK",res.getCode());
		
		devices.clear();
		 dev1 = new Device();
		dev1.setIp("10.0.0.17");
		dev1.setName("test7");
		dev1.setMac("11:22:33:44:55:27");
		dev1.setWlanMac("");
		dev1.setWlanIp("");
		devices.add(dev1);
		 res = r.addDevices(1, devices);
		assertEquals("OK",res.getCode());
	}
}
