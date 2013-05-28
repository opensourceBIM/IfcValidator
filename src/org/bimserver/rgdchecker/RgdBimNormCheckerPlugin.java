package org.bimserver.rgdchecker;
import java.util.Date;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SActionState;
import org.bimserver.interfaces.objects.SExtendedData;
import org.bimserver.interfaces.objects.SExtendedDataSchema;
import org.bimserver.interfaces.objects.SFile;
import org.bimserver.interfaces.objects.SLongActionState;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProgressTopicType;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.log.AccessMethod;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ServiceDescriptor;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.models.store.Trigger;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.services.BimServerClientException;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.plugins.services.NewRevisionHandler;
import org.bimserver.plugins.services.ServicePlugin;
import org.bimserver.shared.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;
import org.codehaus.jettison.json.JSONException;

import com.google.common.base.Charsets;

public class RgdBimNormCheckerPlugin extends ServicePlugin {

	private boolean initialized;

	@Override
	public void init(PluginManager pluginManager) throws PluginException {
		super.init(pluginManager);
		initialized = true;
	}
	
	@Override
	public String getDescription() {
		return "RGD BIM Norm Checker";
	}

	@Override
	public String getDefaultName() {
		return "RGD BIM Norm Checker";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return null;
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public String getTitle() {
		return "RGD BIM Norm Checker";
	}

	@Override
	public void register(PluginConfiguration pluginConfiguration) {
		ServiceDescriptor serviceDescriptor = StoreFactory.eINSTANCE.createServiceDescriptor();
		serviceDescriptor.setProviderName("BIMserver");
		serviceDescriptor.setIdentifier(getClass().getName());
		serviceDescriptor.setName("RGD BIM Norm Checker");
		serviceDescriptor.setDescription("RGD BIM Norm Checker");
		serviceDescriptor.setNotificationProtocol(AccessMethod.INTERNAL);
		serviceDescriptor.setTrigger(Trigger.NEW_REVISION);
		serviceDescriptor.setReadRevision(true);
		final String schemaNamespace = "http://extend.bimserver.org/validationreport";
		serviceDescriptor.setWriteExtendedData(schemaNamespace);
		registerNewRevisionHandler(serviceDescriptor, new NewRevisionHandler() {
			@Override
			public void newRevision(BimServerClientInterface bimServerClientInterface, long poid, long roid, long soid, SObjectType settings) throws ServerException, UserException {
				try {
					Long topicId = bimServerClientInterface.getRegistry().registerProgressOnRevisionTopic(SProgressTopicType.RUNNING_SERVICE, poid, roid, "Running RGD BIM Norm Checker");
					SLongActionState state = new SLongActionState();
					Date startDate = new Date();
					state.setProgress(-1);
					state.setTitle("Bezig...");
					state.setState(SActionState.FINISHED);
					state.setStart(startDate);
					bimServerClientInterface.getRegistry().updateProgressTopic(topicId, state);
					SExtendedDataSchema schema = bimServerClientInterface.getServiceInterface().getExtendedDataSchemaByNamespace(schemaNamespace);

					IfcModelInterface model = bimServerClientInterface.getModel(poid, roid, true);

					ValidationReport validationReport = new ValidationReport();
					
					validationReport.addHeader("Number of objects per type");
					
					int nrIfcProjects = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcProject());
					int nrIfcSites = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcSite());

					validationReport.add(nrIfcProjects == 1 ? Type.SUCCESS : Type.ERROR, -1, "Number of projects", nrIfcProjects + " projects", "Exactly 1 IfcProject object");
					validationReport.add(nrIfcProjects == 1 ? Type.SUCCESS : Type.ERROR, -1, "Number of sites", nrIfcSites + " sites", "Exactly 1 IfcSite object");
					
					SFile file = new SFile();
					file.setMime("application/json; charset=utf-8");
					file.setFilename("validationresults.json");
					file.setData(validationReport.toJson().toString(2).getBytes(Charsets.UTF_8));
					
					file.setOid(bimServerClientInterface.getServiceInterface().uploadFile(file));
					
					SExtendedData extendedData = new SExtendedData();
					extendedData.setTitle("RGD BIM Norm Validation Report");
					extendedData.setSchemaId(schema.getOid());
					extendedData.setFileId(file.getOid());
					
					bimServerClientInterface.getServiceInterface().addExtendedDataToRevision(roid, extendedData);
					
					bimServerClientInterface.getRegistry().unregisterProgressTopic(topicId);
				} catch (PublicInterfaceNotFoundException e1) {
					e1.printStackTrace();
				} catch (BimServerClientException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
}