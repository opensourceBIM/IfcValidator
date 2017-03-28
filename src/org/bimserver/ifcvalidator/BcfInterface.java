package org.bimserver.ifcvalidator;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bimserver.validationreport.Issue;
import org.bimserver.validationreport.IssueContainer;
import org.bimserver.validationreport.IssueContainerSerializer;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueValidationException;
import org.bimserver.validationreport.Type;
import org.opensourcebim.bcf.BcfException;
import org.opensourcebim.bcf.BcfFile;
import org.opensourcebim.bcf.BcfValidationException;
import org.opensourcebim.bcf.TopicFolder;
import org.opensourcebim.bcf.markup.Header;
import org.opensourcebim.bcf.markup.Header.File;
import org.opensourcebim.bcf.markup.Markup;
import org.opensourcebim.bcf.markup.Topic;
import org.opensourcebim.bcf.markup.ViewPoint;

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