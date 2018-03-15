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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueContainerSerializer;
import org.opensourcebim.bcf.BcfException;
import org.opensourcebim.bcf.BcfFile;

public class BcfInterface implements IssueContainerSerializer {

	private BcfFile bcfFile;

	public BcfInterface(CheckerContext translator) {
		bcfFile = new BcfFile();
	}
	
//	@Override
//	public Issue add(Type messageType, String type, String guid, Long oid, String message, Object is, String shouldBe) throws IssueException {
//		TopicFolder topicFolder = bcfFile.createTopicFolder();
//		Topic topic = topicFolder.createTopic();
//		topic.setTitle(message);
//		topic.setGuid(topicFolder.getUuid().toString());
//		topic.setTopicStatus(messageType.toString());
//		topic.setCreationAuthor("Test");
//		topicFolder.setDefaultSnapShotToDummy();
//
//		Issue issue = new Issue(){
//			@Override
//			public void addImage(BufferedImage image) {
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				try {
//					ImageIO.write(image, "png", baos);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				topicFolder.setDefaultSnapShot(baos.toByteArray());
//				
//				topicFolder.addSnapShot("snapshot.png", baos.toByteArray());
//				
//				ViewPoint viewPoint = new ViewPoint();
//				viewPoint.setSnapshot("snapshot.png");
//				viewPoint.setGuid(topicFolder.getUuid().toString());
//				viewPoint.setViewpoint(UUID.randomUUID().toString());
//				topicFolder.getMarkup().getViewpoints().add(viewPoint);
//			}
//		};
//		
//		Markup markup = topicFolder.getMarkup();
//		Header header = new Header();
//		markup.setHeader(header);
//		List<File> files = header.getFile();
//		
//		File file = new File();
//		file.setIfcSpatialStructureElement(guid);
//		files.add(file);
//		
//		GregorianCalendar now = new GregorianCalendar();
//		try {
//			topicFolder.getMarkup().getTopic().setCreationDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(now));
//		} catch (DatatypeConfigurationException e) {
//			throw new IssueException(e);
//		}
//		return issue;
//	}

	@Override
	public byte[] getBytes(IssueContainer issueContainer) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bcfFile.write(baos);
		} catch (BcfException e) {
			throw new IOException(e);
		}
		return baos.toByteArray();
	}
}