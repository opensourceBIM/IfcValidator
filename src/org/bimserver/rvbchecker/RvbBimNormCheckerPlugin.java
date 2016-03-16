package org.bimserver.rvbchecker;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bimserver.PluginSettingsDefinition;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.interfaces.objects.SObjectType;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.models.geometry.GeometryInfo;
import org.bimserver.models.geometry.Vector3f;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc2x3tc1.IfcBuilding;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcDirection;
import org.bimserver.models.ifc2x3tc1.IfcGeometricRepresentationContext;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcProject;
import org.bimserver.models.ifc2x3tc1.IfcRepresentationContext;
import org.bimserver.models.ifc2x3tc1.IfcSIPrefix;
import org.bimserver.models.ifc2x3tc1.IfcSIUnit;
import org.bimserver.models.ifc2x3tc1.IfcSIUnitName;
import org.bimserver.models.ifc2x3tc1.IfcSite;
import org.bimserver.models.ifc2x3tc1.IfcUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnitAssignment;
import org.bimserver.models.ifc2x3tc1.IfcUnitEnum;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.services.AbstractAddExtendedDataService;
import org.bimserver.plugins.services.BimServerClientInterface;
import org.bimserver.utils.IfcUtils;
import org.bimserver.validationreport.Type;
import org.bimserver.validationreport.ValidationReport;
import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

public class RvbBimNormCheckerPlugin extends AbstractAddExtendedDataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RvbBimNormCheckerPlugin.class);
	private Map<String, String> translations = new HashMap<>();

	public RvbBimNormCheckerPlugin() {
		super("RvbBimNormChecker", "http://extend.bimserver.org/validationreport");
	}

	@Override
	public String getTitle() {
		return "RGD BIM Norm Checker";
	}

	@Override
	public void newRevision(RunningService runningService, BimServerClientInterface bimServerClientInterface, long poid, long roid, String userToken, long soid, SObjectType settings) throws Exception {
		String title = "RGD BIM Norm Checker";

		runningService.updateProgress(0);
		
		SProject project = bimServerClientInterface.getBimsie1ServiceInterface().getProjectByPoid(poid);

		IfcModelInterface model = bimServerClientInterface.getModel(project, roid, true, false, true);

		ValidationReport validationReport = new ValidationReport();
		
		check227IfcObjects(model, validationReport);
		
		addExtendedData(validationReport.toJson(new ObjectMapper()).toString().getBytes(Charsets.UTF_8), "validationresults.json", title, "application/json; charset=utf-8", bimServerClientInterface, roid);
		
		runningService.updateProgress(100);
	}
	
	private void check227IfcObjects(IfcModelInterface model, ValidationReport validationReport) {
		check2271IfcProject(model, validationReport);
		check2272IfcSite(model, validationReport);
		check2273Bouwwerk(model, validationReport);
		check2274Bouwlaag(model, validationReport);
		check2275IfcSpaces(model, validationReport);
		check2276(model, validationReport);
		check2277(model, validationReport);
		check2278(model, validationReport);
		check2279(model, validationReport);
		check22710(model, validationReport);
		check22711(model, validationReport);
	}
	
	private void check2272IfcSite(IfcModelInterface model, ValidationReport validationReport) {
		validationReport.addHeader(translate("SITE"));

		int nrIfcSites = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcSite());
		validationReport.add(nrIfcSites == 1 ? Type.SUCCESS : Type.ERROR, -1, "Number of sites", nrIfcSites + " sites", "Exactly 1 IfcSite object");
		
		// TODO check site contours, not possible without kadastrale data
		// TODO has geographic location
		
		for (IfcSite ifcSite : model.getAll(IfcSite.class)) {
			try {
				checkKadastraleAanduidingen(ifcSite);
				validationReport.add(Type.SUCCESS, ifcSite.getOid(), "Kadastrale aanduiding", "Valid", "Valid");
			} catch (ValidationException e) {
				validationReport.add(Type.ERROR, ifcSite.getOid(), "Kadastrale aanduiding", "Invalid", "Valid");
			}
			checkLatLonElevation(ifcSite, validationReport);
		}
	}
	
	private void check2273Bouwwerk(IfcModelInterface model, ValidationReport validationReport) {
		validationReport.addHeader(translate("BUILDING"));

		int nrBuildings = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcBuilding());
		validationReport.add(nrBuildings > 0 ? Type.SUCCESS : Type.ERROR, -1, "Number of buildings", nrBuildings + " IfcBuilding objects", "> 0 IfcBuilding objects");
	}
	
	private void check2275IfcSpaces(IfcModelInterface model, ValidationReport validationReport) {
		
	}
	
	private void check2276(IfcModelInterface model, ValidationReport validationReport) {
		
	}
	
	private void check2277(IfcModelInterface model, ValidationReport validationReport) {
		
	}
	
	private void check2278(IfcModelInterface model, ValidationReport validationReport) {
		validationReport.addHeader(translate("BUILDING STOREYS"));
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			if (ifcProduct instanceof IfcSite || ifcProduct instanceof IfcBuilding) {
				continue;
				// Skip
			}
			IfcBuildingStorey ifcBuildingStorey = IfcUtils.getIfcBuildingStorey(ifcProduct);
			if (ifcBuildingStorey == null) {
				validationReport.add(Type.ERROR, ifcProduct.getOid(), "Object " + getObjectIdentifier(ifcProduct) + " must be linked to a building storey", "None", "Building storey");
			}
		}
	}
	
	private void check2279(IfcModelInterface model, ValidationReport validationReport) {
		// These are not automatically checkable as we do not know which elements are supposed to be constructive
	}
	
	private void check22710(IfcModelInterface model, ValidationReport validationReport) {
		// These are not automatically checkable as we do not know which elements are electrical
	}
	
	private void check22711(IfcModelInterface model, ValidationReport validationReport) {
		
	}
	
	private void check2274Bouwlaag(IfcModelInterface model, ValidationReport validationReport) {
		validationReport.addHeader(translate("BUILDING STOREYS"));
		
		int nrBuildingStoreys = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcBuildingStorey());
		validationReport.add(nrBuildingStoreys > 0 ? Type.SUCCESS : Type.ERROR, -1, "Number of building storeys", nrBuildingStoreys + " IfcBuildingStorey objects", "> 0 IfcBuildingStorey objects");
		
		// TODO check whether all objects are linked to storeys
		
		Map<Integer, IfcBuildingStorey> mapped = new TreeMap<>();
		for (IfcBuildingStorey ifcBuildingStorey : model.getAll(IfcBuildingStorey.class)) {
			String name = ifcBuildingStorey.getName();
			if (name.contains(" ")) {
				String[] split = name.split(" ");
				String number = split[0];
				try {
					if (!Character.isDigit(number.charAt(number.length() -1))) {
						// Must be a sub-storey, skip it for now
					} else {
						int storeyNumber = Integer.parseInt(number);
						if (mapped.containsKey(storeyNumber)) {
							validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Duplicate storey name", ifcBuildingStorey.getName(), "");
						} else {
							mapped.put(storeyNumber, ifcBuildingStorey);
							validationReport.add(Type.SUCCESS, ifcBuildingStorey.getOid(), "Valid building name", ifcBuildingStorey.getName(), "");
						}
					}
				} catch (NumberFormatException e) {
					validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Invalid building name, invalid number " + split[0], ifcBuildingStorey.getName(), "");
				}
			} else {
				validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Invalid building name, no spaces", ifcBuildingStorey.getName(), "");
			}
		}
		double lastZ = -1;
		boolean increasingWithHeight = true;
		for (int number : mapped.keySet()) {
			IfcBuildingStorey ifcBuildingStorey = mapped.get(number);
			double minZ = Double.MAX_VALUE;
			double maxZ = -Double.MAX_VALUE;
			for (IfcProduct ifcProduct : IfcUtils.getChildren(ifcBuildingStorey)) {
				GeometryInfo geometry = ifcProduct.getGeometry();
				if (geometry != null) {
					Vector3f min = geometry.getMinBounds();
					Vector3f max = geometry.getMaxBounds();
					if (min.getZ() < minZ) {
						minZ = min.getZ();
					}
					if (max.getZ() > maxZ) {
						maxZ = max.getZ();
					}
				}
			}
			double aabbCenterZ = minZ + (maxZ - minZ) / 2d;
			IfcBuildingStorey lastStorey = null;
			if (lastZ == -1 || aabbCenterZ > lastZ) {
				lastZ = aabbCenterZ;
			} else {
				increasingWithHeight = false;
				validationReport.add(Type.ERROR, ifcBuildingStorey.getOid(), "Building storey " + getObjectIdentifier(ifcBuildingStorey) + " seems to be lower than " + getObjectIdentifier(lastStorey), ifcBuildingStorey.getName(), "");
			}
		}
		if (increasingWithHeight) {
			validationReport.add(Type.SUCCESS, -1, "Storeys seem to be increasing with z-value and naming", "", "");
		}
	}

	private void checkLatLonElevation(IfcSite ifcSite, ValidationReport validationReport) {
		validationReport.addHeader(translate("GEO_LOCATION"));

		// Only checking whether this data is available
		
		if (ifcSite.getRefLatitude() != null) {
			// TODO check whether this is a valid WSG84
			validationReport.add(Type.SUCCESS, ifcSite.getOid(), "RefLatitude", "Not null", "Not null");
		} else {
			validationReport.add(Type.ERROR, ifcSite.getOid(), "RefLatitude", null, "Not null");
		}
		if (ifcSite.getRefLongitude() != null) {
			// TODO check whether this is a valid WSG84
			validationReport.add(Type.SUCCESS, ifcSite.getOid(), "RefLongitude", "Not null", "Not null");
		} else {
			validationReport.add(Type.ERROR, ifcSite.getOid(), "RefLongitude", null, "Not null");
		}
		if (ifcSite.isSetRefElevation()) {
			validationReport.add(Type.SUCCESS, ifcSite.getOid(), "RefElevation", "Not null", "Not null");
		} else {
			validationReport.add(Type.ERROR, ifcSite.getOid(), "RefElevation", null, "Not null");
		}
	}

	private String getObjectIdentifier(IfcProduct ifcProduct) {
		String name = ifcProduct.getName();
		if (name != null && !name.trim().equals("")) {
			return name;
		}
		String guid = ifcProduct.getGlobalId();
		if (guid != null && !guid.trim().equals("")) {
			return guid;
		}
		return ifcProduct.eClass().getName() + " " + ifcProduct.getOid();
	}
	
	private void checkKadastraleAanduidingen(IfcSite ifcSite) throws ValidationException {
		String name = ifcSite.getName();
		String[] split = name.split("-");
		for (String part : split) {
			if (part.contains(" ")) {
				String[] spacesSplit = part.split(" ");
				String number = spacesSplit[spacesSplit.length - 1];
				try {
					Integer.parseInt(number);
				} catch (NumberFormatException e) {
					throw new ValidationException("perceelsnummer not a number");
				}
			}
		}
	}

	private void check2271IfcProject(IfcModelInterface model, ValidationReport validationReport) {
		validationReport.addHeader(translate("PROJECT"));

		int nrIfcProjects = model.count(Ifc2x3tc1Package.eINSTANCE.getIfcProject());
		validationReport.add(nrIfcProjects == 1 ? Type.SUCCESS : Type.ERROR, -1, "Number of projects", nrIfcProjects + " projects", "Exactly 1 IfcProject object");
		
		for (IfcProject ifcProject : model.getAll(IfcProject.class)) {
			checkIfcProjectRepresentations(validationReport, ifcProject);
			checkIfcProjectUnits(validationReport, ifcProject);
		}
	}

	private void checkIfcProjectUnits(ValidationReport validationReport, IfcProject ifcProject) {
		validationReport.addHeader(translate("UNITS"));

		IfcUnitAssignment unitsInContext = ifcProject.getUnitsInContext();
		
		boolean lengthUnitFound = false;
		boolean volumeUnitFound = false;
		boolean areaUnitFound = false;
		
		for (IfcUnit ifcUnit : unitsInContext.getUnits()) {
			if (ifcUnit instanceof IfcSIUnit) {
				IfcSIUnit ifcSIUnit = (IfcSIUnit)ifcUnit;
				if (ifcSIUnit.getUnitType() == IfcUnitEnum.LENGTHUNIT) {
					lengthUnitFound = true;
					boolean metres = ifcSIUnit.getName() == IfcSIUnitName.METRE;
					boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.MILLI || ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
					validationReport.add(lengthUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Length unit definition", lengthUnitFound, "Found");
					validationReport.add(metres ? Type.SUCCESS : Type.ERROR, -1, "Length unit", metres, "Metres");
					validationReport.add(rightPrefix ? Type.SUCCESS : Type.ERROR, -1, "Length unit prefix", ifcSIUnit.getPrefix(), "None or millis");
				} else if (ifcSIUnit.getUnitType() == IfcUnitEnum.AREAUNIT) {
					areaUnitFound = true;
					boolean metres = ifcSIUnit.getName() == IfcSIUnitName.SQUARE_METRE;
					boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
					validationReport.add(areaUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Area unit definition", areaUnitFound, "Found");
					validationReport.add(metres ? Type.SUCCESS : Type.ERROR, -1, "Area unit", metres, "Metres squared");
					validationReport.add(rightPrefix ? Type.SUCCESS : Type.ERROR, -1, "Area unit prefix", ifcSIUnit.getPrefix(), "None");
				} else if (ifcSIUnit.getUnitType() == IfcUnitEnum.VOLUMEUNIT) {
					volumeUnitFound = true;
					boolean metres = ifcSIUnit.getName() == IfcSIUnitName.CUBIC_METRE;
					boolean rightPrefix = ifcSIUnit.getPrefix() == IfcSIPrefix.NULL;
					validationReport.add(volumeUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Volume unit definition", volumeUnitFound, "Found");
					validationReport.add(metres ? Type.SUCCESS : Type.ERROR, -1, "Volume unit", metres, "Cubic metres");
					validationReport.add(rightPrefix ? Type.SUCCESS : Type.ERROR, -1, "Volume unit prefix", ifcSIUnit.getPrefix(), "None");
				}
			}
		}
		if (!lengthUnitFound) {
			validationReport.add(lengthUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Length unit definition", lengthUnitFound, "Found");
		}
		if (!areaUnitFound) {
			validationReport.add(areaUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Area unit definition", areaUnitFound, "Found");
		}
		if (!volumeUnitFound) {
			validationReport.add(volumeUnitFound ? Type.SUCCESS : Type.ERROR, -1, "Volume unit definition", volumeUnitFound, "Found");
		}
	}

	private void checkIfcProjectRepresentations(ValidationReport validationReport, IfcProject ifcProject) {
		validationReport.addHeader(translate("REPRESENTATION"));
		EList<IfcRepresentationContext> representationContexts = ifcProject.getRepresentationContexts();
		if (representationContexts.isEmpty()) {
			validationReport.add(Type.ERROR, ifcProject.getOid(), translate("IFC_PROJECT_NUMBER_OF_REPRESENTATION_CONTEXTS"), "0", "> 0");
		} else {
			IfcDirection trueNorth = null;
			for (IfcRepresentationContext ifcRepresentationContext : representationContexts) {
				if (ifcRepresentationContext instanceof IfcGeometricRepresentationContext) {
					IfcGeometricRepresentationContext ifcGeometricRepresentationContext = (IfcGeometricRepresentationContext)ifcRepresentationContext;
					if (ifcGeometricRepresentationContext.getTrueNorth() != null) {
						trueNorth = ifcGeometricRepresentationContext.getTrueNorth();
					}
				}
			}
			String stringVersion = "null";
			if (trueNorth != null) {
				Joiner joiner = Joiner.on(", ").skipNulls();
				stringVersion = joiner.join(trueNorth.getDirectionRatios());
			}
			validationReport.add(trueNorth != null ? Type.SUCCESS : Type.ERROR, -1, "TrueNorth", stringVersion, "Set");
		}
	}

	private String translate(String key) {
		String value = translations.get(key);
		if (value == null) {
			LOGGER.info("Missing translations for key " + key);
			return key;
		}
		return value;
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		PluginSettingsDefinition settings = new PluginSettingsDefinition();
		settings.addHeader("Project");
		settings.addBoolean("One IfcProject", "", true);
		settings.addHeader("Site");
		settings.addBoolean("One IfcSite", "", true);

		ObjectDefinition objectDefinition = StoreFactory.eINSTANCE.createObjectDefinition();	
		
		return objectDefinition;
	}
}