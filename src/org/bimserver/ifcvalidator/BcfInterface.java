package org.bimserver.ifcvalidator;

import java.awt.image.BufferedImage;

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
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bimserver.emf.IdEObject;
import org.bimserver.validationreport.Issue;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueContainerSerializer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.RootIssueContainer;
import org.bimserver.validationreport.Type;
import org.opensourcebim.bcf.BcfException;
import org.opensourcebim.bcf.BcfFile;
import org.opensourcebim.bcf.TopicFolder;
import org.opensourcebim.bcf.markup.Header;
import org.opensourcebim.bcf.markup.Markup;
import org.opensourcebim.bcf.markup.Topic;
import org.opensourcebim.bcf.markup.ViewPoint;
import org.slf4j.LoggerFactory;

public class BcfInterface implements IssueContainerSerializer {

	private static DatatypeFactory DATATYPE_FACTORY;
	private BcfFile bcfFile;
	private RootIssueContainer rootIssueContainer;
	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BcfFile.class);

	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			LOGGER.error("", e);
		}
	}
	
	public BcfInterface(CheckerContext translator) {
		bcfFile = new BcfFile();
	}
	
	public Issue add(Type messageType, String type, String guid, Long oid, String message, Object is, String shouldBe, String author) throws IssueException {
		if (messageType == Type.SUCCESS) {
			// Not really uesful in a BCF, skip it
			return null;
		}
		TopicFolder topicFolder = bcfFile.createTopicFolder();
		Topic topic = topicFolder.createTopic();
		topic.setTitle(message);
		topic.setGuid(topicFolder.getUuid().toString());
		topic.setTopicType(messageType.name());
		topic.setTopicStatus(messageType.toString());
		topic.setCreationAuthor(author);
		topicFolder.setDefaultSnapShotToDummy();

		Issue issue = new Issue(){
			@Override
			public void setImage(BufferedImage image) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					ImageIO.write(image, "png", baos);
				} catch (IOException e) {
					e.printStackTrace();
				}

				topicFolder.setDefaultSnapShot(baos.toByteArray());
				
				topicFolder.addSnapShot("snapshot.png", baos.toByteArray());
				
				ViewPoint viewPoint = new ViewPoint();
				viewPoint.setSnapshot("snapshot.png");
				viewPoint.setGuid(topicFolder.getUuid().toString());
				viewPoint.setViewpoint(UUID.randomUUID().toString());
				topicFolder.getMarkup().getViewpoints().add(viewPoint);
			}
		};
		
		Markup markup = topicFolder.getMarkup();
		Header header = new Header();
		markup.setHeader(header);
		List<Header.File> files = header.getFile();
		
		Header.File file = new Header.File();
		file.setIfcSpatialStructureElement(guid);
		IdEObject ifcProject = rootIssueContainer.getValidationMetaData().getIfcProject();
		if (ifcProject != null) {
			file.setIfcProject((String) ifcProject.eGet(ifcProject.eClass().getEStructuralFeature("GlobalId")));
		}
		file.setIsExternal(true);
		file.setFilename(rootIssueContainer.getValidationMetaData().getFileName());
		file.setDate(rootIssueContainer.getValidationMetaData().getFileDate());
		file.setReference(rootIssueContainer.getValidationMetaData().getRemoteReference());
		files.add(file);
		
		GregorianCalendar now = new GregorianCalendar();
		topicFolder.getMarkup().getTopic().setCreationDate(DATATYPE_FACTORY.newXMLGregorianCalendar(now));
		return issue;
	}

	@Override
	public byte[] getBytes(RootIssueContainer issueContainer) throws IOException {
		this.rootIssueContainer = issueContainer;
		processContainer(issueContainer);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bcfFile.write(baos);
		} catch (BcfException e) {
			throw new IOException(e);
		}
		return baos.toByteArray();
	}

	private void processContainer(IssueContainer issueContainer) {
		Collection<Issue> list = issueContainer.list();
		for (Issue issue : list) {
			try {
				if (issue instanceof IssueContainer) {
					IssueContainer container = (IssueContainer)issue;
					processContainer(container);
				} else {
					add(issue.getType(), null, null, null, issue.getMessage(), issue.getIs(), "" + issue.getShouldBe(), issue.getAuthor());
				}
			} catch (IssueException e) {
				e.printStackTrace();
			} catch (Exception e) {
				LOGGER.info(issue.toString());
				e.printStackTrace();
			}
		}
	}
}