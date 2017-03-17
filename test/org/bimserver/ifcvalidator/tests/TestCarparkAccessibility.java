package org.bimserver.ifcvalidator.tests;

import java.nio.file.Paths;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.Tester;
import org.bimserver.interfaces.objects.SDeserializerPluginConfiguration;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.plugins.services.Flow;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.test.AllTests;
import org.bimserver.test.TestWithEmbeddedServer;
import org.bimserver.validationreport.IssueContainer;
import org.junit.Assert;
import org.junit.Test;

public class TestCarparkAccessibility extends TestWithEmbeddedServer {
	@Test
	public void testTooFew() {
		try {
			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());
			client.checkin(newProject.getOid(), "test", deserializer.getOid(), false, Flow.SYNC, Paths.get("D:\\Dropbox\\Shared\\Singapore Code Compliance Share\\09_b_02_fail_tooFew.ifc"));
			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
			
			Tester tester = new Tester();
			tester.test(model, "ACCESSIBILITY", "CARPARKS");
			IssueContainer issueContainer = tester.getIssueContainer();
			
			Assert.assertEquals(21, issueContainer.list().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testTooSmall() {
		try {
			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());
			client.checkin(newProject.getOid(), "test", deserializer.getOid(), false, Flow.SYNC, Paths.get("D:\\Dropbox\\Shared\\Singapore Code Compliance Share\\09_b_02_fail_tooSmall.ifc"));
			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
			
			Tester tester = new Tester();
			tester.test(model, "ACCESSIBILITY", "CARPARKS");
			IssueContainer issueContainer = tester.getIssueContainer();
			
			Assert.assertEquals(21, issueContainer.list().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testTooPass() {
		try {
			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());
			client.checkin(newProject.getOid(), "test", deserializer.getOid(), false, Flow.SYNC, Paths.get("D:\\Dropbox\\Shared\\Singapore Code Compliance Share\\09_b_02_pass.ifc"));
			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
			
			Tester tester = new Tester();
			tester.test(model, "ACCESSIBILITY", "CARPARKS");
			IssueContainer issueContainer = tester.getIssueContainer();

			Assert.assertEquals(0, issueContainer.list().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}