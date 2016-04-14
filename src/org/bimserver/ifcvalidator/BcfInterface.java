package org.bimserver.ifcvalidator;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bimserver.validationreport.IssueException;
import org.bimserver.validationreport.IssueInterface;
import org.bimserver.validationreport.IssueValidationException;
import org.bimserver.validationreport.Type;
import org.opensourcebim.bcf.BcfException;
import org.opensourcebim.bcf.BcfFile;
import org.opensourcebim.bcf.BcfValidationException;
import org.opensourcebim.bcf.TopicFolder;

public class BcfInterface implements IssueInterface {

	private BcfFile bcfFile;

	public BcfInterface(Translator translator) {
		bcfFile = new BcfFile();
	}
	
	@Override
	public void add(Type messageType, String type, String guid, Long oid, String message, Object is, String shouldBe) throws IssueException {
		if (messageType == Type.SUCCESS) {
			// Ignore, no SUCCESSES in BCF
		} else if (messageType == Type.ERROR) {
			TopicFolder topicFolder = bcfFile.createTopicFolder();
			topicFolder.getTopic().setTitle(message);
			topicFolder.getTopic().setGuid(UUID.randomUUID().toString());
			topicFolder.getTopic().setCreationAuthor("");
			topicFolder.setDefaultSnapShotToDummy();
			
			GregorianCalendar now = new GregorianCalendar();
			try {
				topicFolder.getTopic().setCreationDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(now));
			} catch (DatatypeConfigurationException e) {
				throw new IssueException(e);
			}
		} else {
			throw new IssueException("Unimplemented type " + type);
		}
	}

	@Override
	public void addHeader(String translate) {
		// No headers in BCF
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			bcfFile.write(baos);
		} catch (BcfException e) {
			throw new IOException(e);
		}
		return baos.toByteArray();
	}

	@Override
	public void validate() throws IssueValidationException {
		try {
			bcfFile.validate();
		} catch (BcfValidationException e) {
			throw new IssueValidationException();
		}
	}

	@Override
	public void setCheckValid(String identifier, boolean valid) {
	}

	@Override
	public void add(Type messageType, String message, Object is, String shouldBe) throws IssueException {
		add(messageType, null, null, null, message, is, shouldBe);
	}
}