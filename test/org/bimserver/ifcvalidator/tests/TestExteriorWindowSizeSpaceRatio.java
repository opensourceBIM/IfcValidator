//package org.bimserver.ifcvalidator.tests;
//
///******************************************************************************
// * Copyright (C) 2009-2018  BIMserver.org
// * 
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// * 
// * You should have received a copy of the GNU Affero General Public License
// * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
// *****************************************************************************/
//
//import java.net.URL;
//
//import org.bimserver.emf.IfcModelInterface;
//import org.bimserver.ifcvalidator.Tester;
//import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
//import org.bimserver.interfaces.objects.SProject;
//import org.bimserver.plugins.services.BimServerClientInterface;
//import org.bimserver.plugins.services.CheckinProgressHandler;
//import org.bimserver.plugins.services.Flow;
//import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
//import org.bimserver.test.AllTests;
//import org.bimserver.test.TestWithEmbeddedServer;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class TestExteriorWindowSizeSpaceRatio extends TestWithEmbeddedServer {
//	@Test
//	public void test() {
//		try {
//			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
//			
//			// Create a new project
//			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
//			
//			// Get the appropriate deserializer
//			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());
//
//			// Checkin the file
//			client.checkin(newProject.getOid(), "test", deserializer.getOid(), new URL("https://github.com/opensourceBIM/TestFiles/raw/master/TestData/data/AC11-Institute-Var-2-IFC.ifc"), new CheckinProgressHandler() {
//				@Override
//				public void progress(String title, int progress) {
//				}
//			});
//
//			// Refresh project info
//			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
//			
//			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
//			
//			Tester tester = new Tester();
//			tester.test(model, "GEOMETRY", "RATIOS");
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail();
//		}
//	}
//}