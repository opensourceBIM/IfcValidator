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
//import java.nio.file.Paths;
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
//import org.bimserver.validationreport.IssueContainer;
//import org.junit.Assert;
//import org.junit.Test;
//
//public class TestCarparkAccessibility extends TestWithEmbeddedServer {
//	@Test
//	public void testTooFew() {
//		try {
//			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
//			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
//			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());
//			client.checkinSync(newProject.getOid(), "test", deserializer.getOid(), Paths.get("D:\\Dropbox\\Shared\\Singapore Code Compliance Share\\09_b_02_fail_tooFew.ifc"), new CheckinProgressHandler() {
//				@Override
//				public void progress(String title, int progress) {
//				}
//			});
//			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
//			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
//			
//			Tester tester = new Tester();
//			tester.test(model, "ACCESSIBILITY", "CARPARKS");
//			IssueContainer issueContainer = tester.getIssueContainer();
//			
//			Assert.assertEquals(21, issueContainer.list().size());
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//	
//	@Test
//	public void testTooSmall() {
//		try {
//			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
//			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
//			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());
//			client.checkinSync(newProject.getOid(), "test", deserializer.getOid(), Paths.get("D:\\Dropbox\\Shared\\Singapore Code Compliance Share\\09_b_02_fail_tooSmall.ifc"), new CheckinProgressHandler() {
//				@Override
//				public void progress(String title, int progress) {
//				}
//			});
//			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
//			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
//			
//			Tester tester = new Tester();
//			tester.test(model, "ACCESSIBILITY", "CARPARKS");
//			IssueContainer issueContainer = tester.getIssueContainer();
//			
//			Assert.assertEquals(21, issueContainer.list().size());
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//
//	@Test
//	public void testTooPass() {
//		try {
//			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
//			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
//			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());
//			client.checkinSync(newProject.getOid(), "test", deserializer.getOid(), Paths.get("D:\\Dropbox\\Shared\\Singapore Code Compliance Share\\09_b_02_pass.ifc"), new CheckinProgressHandler() {
//				@Override
//				public void progress(String title, int progress) {
//				}
//			});
//			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
//			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
//			
//			Tester tester = new Tester();
//			tester.test(model, "ACCESSIBILITY", "CARPARKS");
//			IssueContainer issueContainer = tester.getIssueContainer();
//
//			Assert.assertEquals(0, issueContainer.list().size());
//		} catch (Exception e) {
//			e.printStackTrace();
//			Assert.fail(e.getMessage());
//		}
//	}
//}