package org.bimserver.ifcvalidator;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.bimserver.bimbots.BimBotsException;
import org.bimserver.bimbots.BimBotsInput;
import org.bimserver.bimbots.BimBotsOutput;
import org.bimserver.bimbots.BimBotsServiceInterface;
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
import org.bimserver.plugins.SchemaName;
import org.bimserver.plugins.services.AbstractAddExtendedDataService;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.shared.exceptions.PluginException;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueContainerSerializer;
import org.bimserver.validationreport.IssueException;

public abstract class AbstractIfcValidatorPlugin extends AbstractAddExtendedDataService implements BimBotsServiceInterface {

	private final ModelCheckerRegistry modelCheckerRegistry;
	private boolean generateExtendedDataPerCheck = false;
	private SchemaName outputSchema;

	public AbstractIfcValidatorPlugin(SchemaName outputSchema, boolean generateExtendedDataPerCheck, ModelCheckerRegistry modelCheckerRegistry) {
		super(outputSchema.name());
		this.outputSchema = outputSchema;
		this.generateExtendedDataPerCheck = generateExtendedDataPerCheck;
		this.modelCheckerRegistry = modelCheckerRegistry;
	}

	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		super.init(pluginContext);
	}

	protected abstract IssueContainerSerializer createIssueInterface(CheckerContext translator);

	public BimBotsOutput runBimBot(BimBotsInput input, SObjectType settings) throws BimBotsException {
		try {
			IfcModelInterface model = input.getIfcModel();

			PluginConfiguration pluginConfiguration = new PluginConfiguration(settings);

			String language = pluginConfiguration.getString("LANGUAGE");

			String filename = language.toLowerCase() + ".properties";
			Path propertiesFile = getPluginContext().getRootPath().resolve(filename);
			Properties properties = new Properties();
			properties.load(Files.newInputStream(propertiesFile));

			CheckerContext checkerContext = new CheckerContext(filename, properties, getPluginContext().getRootPath());

			IssueContainerSerializer issueContainerSerializer = createIssueInterface(checkerContext);
			IssueContainer issueContainer = new IssueContainer();
			for (String groupIdentifier : modelCheckerRegistry.getGroupIdentifiers()) {
				for (String identifier : modelCheckerRegistry.getIdentifiers(groupIdentifier)) {
					String fullIdentifier = groupIdentifier + "___" + identifier;
					IssueContainer issueContainerGroup = new IssueContainer();
					if (pluginConfiguration.has(fullIdentifier)) {
						if (pluginConfiguration.getBoolean(fullIdentifier)) {
							ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);
							modelCheck.check(model, issueContainerGroup, checkerContext);
						}
					}
					issueContainer.add(issueContainerGroup);
				}
			}

			BimBotsOutput bimBotsOutput = new BimBotsOutput(SchemaName.valueOf(getName()), issueContainerSerializer.getBytes(issueContainer));
			bimBotsOutput.setContentType(getContentType());
			bimBotsOutput.setContentDisposition(getFileName());
			bimBotsOutput.setTitle("IFC Validator");
			return bimBotsOutput;
		} catch (IOException e) {
			throw new BimBotsException(e);
		} catch (IssueException e) {
			throw new BimBotsException(e);
		}
	}

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

		CheckerContext checkerContext = new CheckerContext(filename, properties, getPluginContext().getRootPath());

		IssueContainerSerializer issueContainerSerializer = createIssueInterface(checkerContext);
		IssueContainer issueContainer = new IssueContainer();
		for (String groupIdentifier : modelCheckerRegistry.getGroupIdentifiers()) {
//			boolean headerAdded = false;
			for (String identifier : modelCheckerRegistry.getIdentifiers(groupIdentifier)) {
				String fullIdentifier = groupIdentifier + "___" + identifier;
				if (pluginConfiguration.has(fullIdentifier)) {
					if (pluginConfiguration.getBoolean(fullIdentifier)) {
						// if (!generateExtendedDataPerCheck && !headerAdded) {
						// issueContainerSerializer.addHeader(translator.translate(groupIdentifier
						// + "_HEADER"));
						// }
						ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);
						modelCheck.check(model, issueContainer, checkerContext);
					}
				}
			}
			if (generateExtendedDataPerCheck) {
				addExtendedData(issueContainerSerializer.getBytes(issueContainer), getFileName(), groupIdentifier, getContentType(), bimServerClientInterface, roid);
				issueContainer = new IssueContainer();
			}
		}

		// issueContainerSerializer.validate();

		if (!generateExtendedDataPerCheck) {
			addExtendedData(issueContainerSerializer.getBytes(issueContainer), getFileName(), "IFC Validator", getContentType(), bimServerClientInterface, roid);
		}

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
		CheckerContext checkerContext = new CheckerContext(filename, properties, getPluginContext().getRootPath());

		for (String groupIdentifier : modelCheckerRegistry.getGroupIdentifiers()) {
			for (String identifier : modelCheckerRegistry.getIdentifiers(groupIdentifier)) {
				ModelCheck modelCheck = modelCheckerRegistry.getModelCheck(groupIdentifier, identifier);

				ParameterDefinition parameter = StoreFactory.eINSTANCE.createParameterDefinition();
				parameter.setIdentifier(groupIdentifier + "___" + identifier);
				parameter.setName(modelCheck.getName(checkerContext));
				parameter.setType(booleanType);
				parameter.setRequired(true);
				parameter.setDefaultValue(modelCheck.isEnabledByDefault() ? trueValue : falseValue);
				parameter.setDescription(modelCheck.getDescription(checkerContext));
				objectDefinition.getParameters().add(parameter);
			}
		}

		return objectDefinition;
	}
	
	@Override
	public Set<String> getAvailableOutputs() {
		return Collections.singleton(outputSchema.name());
	}
	
	@Override
	public Set<String> getAvailableInputs() {
		return Collections.singleton(SchemaName.IFC_STEP_2X3TC1.name());
	}
}