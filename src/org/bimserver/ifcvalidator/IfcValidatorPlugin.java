package org.bimserver.ifcvalidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifcvalidator.checks.ModelCheck;
import org.bimserver.ifcvalidator.checks.ModelCheckerRegistry;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.store.BooleanType;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ParameterDefinition;
import org.bimserver.models.store.PrimitiveDefinition;
import org.bimserver.models.store.PrimitiveEnum;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.services.AbstractAddExtendedDataService;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.validationreport.ValidationReport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;

public class IfcValidatorPlugin extends AbstractAddExtendedDataService {

	private final ModelCheckerRegistry modelCheckerRegistry;
	private Translator translator;

	public IfcValidatorPlugin() {
		super("IFC Validator", "http://extend.bimserver.org/validationreport");
		
		modelCheckerRegistry = new ModelCheckerRegistry();
	}
	
	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		super.init(pluginContext);
		Path propertiesFile = pluginContext.getRootPath().resolve("en.properties");
		Properties properties = new Properties();
		try {
			properties.load(Files.newInputStream(propertiesFile));
			translator = new Translator(properties);
		} catch (IOException e) {
			throw new PluginException(e);
		}
	}

	@Override
	public void newRevision(RunningService runningService, BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings) throws Exception {
		runningService.updateProgress(0);
		
		SProject project = bimServerClientInterface.getBimsie1ServiceInterface().getProjectByPoid(poid);
		IfcModelInterface model = bimServerClientInterface.getModel(project, roid, true, false, true);
		ValidationReport validationReport = new ValidationReport();

		PluginConfiguration pluginConfiguration = new PluginConfiguration(settings);
		for (String groupIdentifier : modelCheckerRegistry.getGroupIdentifiers()) {
			validationReport.addHeader(translator.translate(groupIdentifier + "_HEADER"));
			for (String identifier : modelCheckerRegistry.getIdentifiers(groupIdentifier)) {
				String fullIdentifier = groupIdentifier + "___" + identifier;
				if (pluginConfiguration.has(fullIdentifier)) {
					if (pluginConfiguration.getBoolean(fullIdentifier)) {
						ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);
						modelCheck.check(model, validationReport, translator);
					}
				}
			}
		}
		
		addExtendedData(validationReport.toJson(new ObjectMapper()).toString().getBytes(Charsets.UTF_8), "validationresults.json", getTitle(), "application/json; charset=utf-8", bimServerClientInterface, roid);
		
		runningService.updateProgress(100);
	}
	
	@Override
	public ObjectDefinition getSettingsDefinition() {
		ObjectDefinition objectDefinition = StoreFactory.eINSTANCE.createObjectDefinition();	
		
		PrimitiveDefinition booleanType = StoreFactory.eINSTANCE.createPrimitiveDefinition();
		booleanType.setType(PrimitiveEnum.BOOLEAN);

		BooleanType falseValue = StoreFactory.eINSTANCE.createBooleanType();
		falseValue.setValue(false);

		BooleanType trueValue = StoreFactory.eINSTANCE.createBooleanType();
		trueValue.setValue(true);

		for (String groupIdentifier : modelCheckerRegistry.getGroupIdentifiers()) {
			for (String identifier : modelCheckerRegistry.getIdentifiers(groupIdentifier)) {
				ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);

				ParameterDefinition parameter = StoreFactory.eINSTANCE.createParameterDefinition();
				parameter.setIdentifier(groupIdentifier + "___" + identifier);
				parameter.setName(modelCheck.getName(translator));
				parameter.setType(booleanType);
				parameter.setRequired(true);
				parameter.setDefaultValue(modelCheck.isEnabledByDefault() ? trueValue : falseValue);
				parameter.setDescription(modelCheck.getDescription(translator));
				objectDefinition.getParameters().add(parameter);
			}
		}
		
		return objectDefinition;
	}
}