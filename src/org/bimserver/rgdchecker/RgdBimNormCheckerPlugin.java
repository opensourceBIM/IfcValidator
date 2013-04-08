package org.bimserver.rgdchecker;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SActionState;
import org.bimserver.interfaces.objects.SExtendedData;
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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

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
		registerNewRevisionHandler(serviceDescriptor, new NewRevisionHandler() {
			@Override
			public void newRevision(BimServerClientInterface bimServerClientInterface, long poid, long roid, SObjectType settings) throws ServerException, UserException {
				try {
					Long topicId = bimServerClientInterface.getRegistry().registerProgressOnRevisionTopic(SProgressTopicType.RUNNING_SERVICE, poid, roid, "Running RGD BIM Norm Checker");
					SLongActionState state = new SLongActionState();
					state.setProgress(-1);
					state.setTitle("Bezig...");
					state.setState(SActionState.FINISHED);
					bimServerClientInterface.getRegistry().updateProgressTopic(topicId, state);
					
					IfcModelInterface model = bimServerClientInterface.getModel(poid, roid, true);
					
					int nrIfcProjects = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcProject());
					int nrIfcSites = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcSite());
					
					StringBuilder builder = new StringBuilder();
					builder.append(CharStreams.toString(new InputStreamReader(getPluginManager().getPluginContext(RgdBimNormCheckerPlugin.this).getResourceAsInputStream("templates/header.html"))));
					builder.append("<tr><td>Exact 1 project</td><td>" + (nrIfcProjects == 1) + "</td></tr>");
					builder.append("<tr><td>Exact 1 site</td><td>" + (nrIfcSites == 1) + "</td></tr>");
					builder.append(CharStreams.toString(new InputStreamReader(getPluginManager().getPluginContext(RgdBimNormCheckerPlugin.this).getResourceAsInputStream("templates/footer.html"))));
					
					SFile file = new SFile();
					file.setMime("text/html");
					file.setFilename("rgdbimnorm.html");
					file.setData(builder.toString().getBytes(Charsets.UTF_8));
					
					bimServerClientInterface.getService().uploadFile(file);
					
					SExtendedData extendedData = new SExtendedData();
					extendedData.setTitle("RGD BIM Norm Report");
					extendedData.setFileId(file.getOid());
					
					bimServerClientInterface.getService().addExtendedDataToRevision(roid, extendedData);
					
					bimServerClientInterface.getRegistry().unregisterProgressTopic(topicId);
				} catch (PublicInterfaceNotFoundException e1) {
					e1.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (BimServerClientException e) {
					e.printStackTrace();
				}
			}
		});
	}
}