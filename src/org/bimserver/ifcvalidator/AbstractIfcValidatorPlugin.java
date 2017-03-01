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
import org.bimserver.models.store.StringType;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.services.AbstractAddExtendedDataService;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.validationreport.IssueInterface;

public abstract class AbstractIfcValidatorPlugin extends AbstractAddExtendedDataService {

	private final ModelCheckerRegistry modelCheckerRegistry;

	public AbstractIfcValidatorPlugin(String namespace) {
		super(namespace);
		
		modelCheckerRegistry = new ModelCheckerRegistry();
	}
	
	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		super.init(pluginContext);
	}
	
	protected abstract IssueInterface createIssueInterface(Translator translator);

	@Override
	public void newRevision(RunningService runningService, BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings) throws Exception {
		runningService.updateProgress(0);
		
		SProject project = bimServerClientInterface.getServiceInterface().getProjectByPoid(poid);
		IfcModelInterface model = bimServerClientInterface.getModel(project, roid, true, false, true);
		
		PluginConfiguration pluginConfiguration = new PluginConfiguration(settings);

		String language = pluginConfiguration.getString("LANGUAGE");
		
		String filename = language.toLowerCase() + ".properties";
		Path propertiesFile = getPluginContext().getRootPath().resolve(filename);
		Properties properties = new Properties();
		properties.load(Files.newInputStream(propertiesFile));
		Translator translator = new Translator(filename, properties);
		
		IssueInterface issueInterface = createIssueInterface(translator);
		for (String groupIdentifier : modelCheckerRegistry.getGroupIdentifiers()) {
			boolean headerAdded = false;
			for (String identifier : modelCheckerRegistry.getIdentifiers(groupIdentifier)) {
				String fullIdentifier = groupIdentifier + "___" + identifier;
				if (pluginConfiguration.has(fullIdentifier)) {
					if (pluginConfiguration.getBoolean(fullIdentifier)) {
						if (!headerAdded) {
							issueInterface.addHeader(translator.translate(groupIdentifier + "_HEADER"));
						}
						ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);
						boolean check = modelCheck.check(model, issueInterface, translator);
						issueInterface.setCheckValid(fullIdentifier, check);
					}
				}
			}
		}
		
		issueInterface.validate();
		
		addExtendedData(issueInterface.getBytes(), getFileName(), "IFC Validator", getContentType(), bimServerClientInterface, roid);
		
		runningService.updateProgress(100);
	}
	
	public abstract String getContentType();
	public abstract String getFileName();
	
	@Override
	public ObjectDefinition getSettingsDefinition() {
		ObjectDefinition objectDefinition = StoreFactory.eINSTANCE.createObjectDefinition();	
		
		PrimitiveDefinition booleanType = StoreFactory.eINSTANCE.createPrimitiveDefinition();
		booleanType.setType(PrimitiveEnum.BOOLEAN);

		BooleanType falseValue = StoreFactory.eINSTANCE.createBooleanType();
		falseValue.setValue(false);

		BooleanType trueValue = StoreFactory.eINSTANCE.createBooleanType();
		trueValue.setValue(true);
		
		PrimitiveDefinition languageValue = StoreFactory.eINSTANCE.createPrimitiveDefinition();
		languageValue.setType(PrimitiveEnum.STRING);
		
		StringType defaultLanguage = StoreFactory.eINSTANCE.createStringType();
		defaultLanguage.setValue("EN");
		
		ParameterDefinition languageParameter = StoreFactory.eINSTANCE.createParameterDefinition();
		languageParameter.setIdentifier("LANGUAGE");
		languageParameter.setDescription("Language of the output");
		languageParameter.setName("Language");
		languageParameter.setType(languageValue);
		languageParameter.setDefaultValue(defaultLanguage);
		
		objectDefinition.getParameters().add(languageParameter);
		
		String filename = "en.properties";
		Path propertiesFile = getPluginContext().getRootPath().resolve(filename);
		Properties properties = new Properties();
		try {
			properties.load(Files.newInputStream(propertiesFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Translator translator = new Translator(filename, properties);
		
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