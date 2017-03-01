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
import org.bimserver.validationreport.JsonValidationReport;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class TestUnidentifiedSpaces extends TestWithEmbeddedServer {
	@Test
	public void testInstitute() {
		try {
			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());

			client.checkin(newProject.getOid(), "test", deserializer.getOid(), false, Flow.SYNC, Paths.get("C:\\Git\\BIMserver\\TestData\\data\\AC11-Institute-Var-2-IFC - Removed Space.ifc"));

			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
			
			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
			
			Tester tester = new Tester();
			boolean result = tester.test(model, "SPACES", "UNIDENTIFIED");
			ObjectNode json = tester.getJsonValidationReport().toJson(JsonValidationReport.OBJECT_MAPPER);
			System.out.println(json);
			Assert.assertEquals(false, result);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testAnon() {
		try {
			BimServerClientInterface client = AllTests.getFactory().create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
			SProject newProject = client.getServiceInterface().addProject("test" + Math.random(), "ifc2x3tc1");
			SDeserializerPluginConfiguration deserializer = client.getServiceInterface().getSuggestedDeserializerForExtension("ifc", newProject.getOid());

			client.checkin(newProject.getOid(), "test", deserializer.getOid(), false, Flow.SYNC, Paths.get("D:\\anon.ifc"));

			newProject = client.getServiceInterface().getProjectByPoid(newProject.getOid());
			
			IfcModelInterface model = client.getModel(newProject, newProject.getLastRevisionId(), true, false, true);
			
			Tester tester = new Tester();
			boolean result = tester.test(model, "SPACES", "UNIDENTIFIED");
			ObjectNode json = tester.getJsonValidationReport().toJson(JsonValidationReport.OBJECT_MAPPER);
			System.out.println(json);
			Assert.assertEquals(false, result);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}