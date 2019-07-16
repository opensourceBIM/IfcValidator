package org.bimserver.ifcvalidator;

/******************************************************************************
 * Copyright (C) 2009-2018  BIMserver.org
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bimserver.bimbots.BimBotContext;
import org.bimserver.bimbots.BimBotsException;
import org.bimserver.bimbots.BimBotsInput;
import org.bimserver.bimbots.BimBotsOutput;
import org.bimserver.bimbots.BimBotsServiceInterface;
import org.bimserver.database.queries.om.Query;
import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.ifcvalidator.checks.ModelCheck;
import org.bimserver.ifcvalidator.checks.ModelCheckerRegistry;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.store.BooleanType;
import org.bimserver.models.store.IfcHeader;
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
import org.bimserver.validationreport.RootIssueContainer;
import org.bimserver.validationreport.ValidationMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractIfcValidatorPlugin extends AbstractAddExtendedDataService implements BimBotsServiceInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIfcValidatorPlugin.class);
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
	public void init(PluginContext pluginContext, PluginConfiguration systemSettings) throws PluginException {
		super.init(pluginContext, systemSettings);
	}

	protected abstract IssueContainerSerializer createIssueInterface(CheckerContext translator);

	public BimBotsOutput runBimBot(BimBotsInput input, BimBotContext bimBotContext, PluginConfiguration pluginConfiguration) throws BimBotsException {
		try {
			IfcModelInterface model = input.getIfcModel();

			BimBotsOutput bimBotsOutput = new BimBotsOutput(SchemaName.valueOf(getName()), process(model, pluginConfiguration, bimBotContext.getCurrentUser()));
			bimBotsOutput.setContentType(getContentType());
			bimBotsOutput.setContentDisposition(getFileName());
			bimBotsOutput.setTitle("IFC Validator");
			return bimBotsOutput;
		} catch (IssueException e) {
			throw new BimBotsException(e, IfcValidatorErrorCodes.ISSUE_EXCEPTION);
		} catch (IOException e) {
			throw new BimBotsException(e, IfcValidatorErrorCodes.IO_EXCEPTION);
		}
	}

	public byte[] process(IfcModelInterface model, PluginConfiguration pluginConfiguration, String currentUser) throws IssueException, IOException {
		String language = pluginConfiguration.getString("LANGUAGE");

		String filename = language.toLowerCase() + ".properties";
		Path propertiesFile = getPluginContext().getRootPath().resolve(filename);
		Properties properties = new Properties();
		try (InputStream newInputStream = Files.newInputStream(propertiesFile)) {
			properties.load(newInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		CheckerContext checkerContext = new CheckerContext(filename, properties, getPluginContext().getRootPath(), currentUser);
		IssueContainerSerializer issueContainerSerializer = createIssueInterface(checkerContext);
		
		ValidationMetaData validationMetaData = new ValidationMetaData();
		RootIssueContainer issueContainer = new RootIssueContainer();
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

//		issueContainer.dumpSummary();
		
		IfcHeader ifcHeader = model.getModelMetaData().getIfcHeader();
		if (ifcHeader != null) {
			if (ifcHeader.getTimeStamp() != null) {
				validationMetaData.setFileDate(dateToXMLGregorianCalendar(ifcHeader.getTimeStamp(), TimeZone.getDefault()));
			}
			validationMetaData.setFileName(ifcHeader.getFilename());
		}
		validationMetaData.setRemoteReference(getPluginContext().getBasicServerInfo().getSiteAddress());

		List<IdEObject> projects = model.getAll(model.getPackageMetaData().getEClass("IfcProject"));
		if (projects.size() == 1) {
			IdEObject ifcProject = projects.get(0);
			validationMetaData.setIfcProject(ifcProject);
		} else if (projects.isEmpty()) {
			LOGGER.info("No IfcProjects");
		} else {
			LOGGER.info("Too many IfcProjects");
		}

		issueContainer.setValidationMetaData(validationMetaData);
		return issueContainerSerializer.getBytes(issueContainer);
	}
	
	public static XMLGregorianCalendar dateToXMLGregorianCalendar(Date date, TimeZone zone) {
		XMLGregorianCalendar xmlGregorianCalendar = null;
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(date);
		gregorianCalendar.setTimeZone(zone);
		try {
			DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
			xmlGregorianCalendar = dataTypeFactory.newXMLGregorianCalendar(gregorianCalendar);
		} catch (Exception e) {
			System.out.println("Exception in conversion of Date to XMLGregorianCalendar" + e);
		}

		return xmlGregorianCalendar;
	}

	@Override
	public void newRevision(RunningService runningService, BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings) throws Exception {
		runningService.updateProgress(0);

		SProject project = bimServerClientInterface.getServiceInterface().getProjectByPoid(poid);
		IfcModelInterface model = bimServerClientInterface.getModel(project, roid, true, false, true);

		if (!generateExtendedDataPerCheck) {
			addExtendedData(process(model, new PluginConfiguration(settings), runningService.getCurrentUser()), getFileName(), "IFC Validator", getContentType(), bimServerClientInterface, roid);
		}

		runningService.updateProgress(100);
	}

	public abstract String getContentType();

	public abstract String getFileName();

	@Override
	public ObjectDefinition getUserSettingsDefinition() {
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
			try (InputStream newInputStream = Files.newInputStream(propertiesFile)) {
				properties.load(newInputStream);
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		CheckerContext checkerContext = new CheckerContext(filename, properties, getPluginContext().getRootPath(), null);

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

	@Override
	public boolean requiresGeometry() {
		return true;
	}

	@Override
	public boolean needsRawInput() {
		return false;
	}
	
	@Override
	public Query getPreloadQuery(PackageMetaData packageMetaData) {
		return null;
	}

	@Override
	public boolean preloadCompleteModel() {
		return false;
	}
}