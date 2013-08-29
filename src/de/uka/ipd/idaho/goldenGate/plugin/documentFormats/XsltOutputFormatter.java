/*
 * Copyright (c) 2006-, IPD Boehm, Universitaet Karlsruhe (TH) / KIT, by Guido Sautter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Universität Karlsruhe (TH) nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITÄT KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uka.ipd.idaho.goldenGate.plugin.documentFormats;


import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import de.uka.ipd.idaho.gamta.MutableAnnotation;
import de.uka.ipd.idaho.gamta.QueriableAnnotation;
import de.uka.ipd.idaho.gamta.util.AnnotationInputStream;
import de.uka.ipd.idaho.goldenGate.DocumentEditor;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractDocumentFormatProvider;
import de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat;
import de.uka.ipd.idaho.goldenGate.util.ResourceDialog;
import de.uka.ipd.idaho.htmlXmlUtil.accessories.XsltUtils;
import de.uka.ipd.idaho.stringUtils.StringVector;

/**
 * Document format provider enabeling GoldenGATE to transform documents through
 * an arbitrary XSLT stylesheet on output, writing the transformation result to
 * the specified character stream instead of the plain XML representation of the
 * document being stored. This document format provider manages the XSLT
 * stylesheets deposited in its data path in the same way as a resource manager
 * does with resources. With JRE's older than version 1.5.x, it requires the
 * Apache Xalan XML/XSLT engine on the class path, most sensibly in its '...Bin'
 * folder.
 * 
 * @author sautter
 */
public class XsltOutputFormatter extends AbstractDocumentFormatProvider {
	
	private static final String[] FILE_EXTENSIONS = {"xml"};
	
	public XsltOutputFormatter() {}
	
	/* 
	 * @see de.uka.ipd.idaho.goldenGate.plugins.GoldenGatePlugin#getPluginName()
	 */
	public String getPluginName() {
		return "XSLT Dynamic Document Format";
	}
	
	/*
	 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormatProvider#getFileExtensions()
	 */
	public String[] getFileExtensions() {
		return FILE_EXTENSIONS;
	}
	
	/*
	 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormatProvider#getFormatForFileExtension(java.lang.String)
	 */
	public DocumentFormat getFormatForFileExtension(String fileExtension) {
		String extension = fileExtension.toLowerCase();
		if (extension.startsWith("."))
			extension = extension.substring(1);
		if ("xml".equalsIgnoreCase(extension))
			return new XsltDynamicDocumentFormat();
		else return null;
	}
	
	/*
	 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormatProvider#getLoadFormatNames()
	 */
	public String[] getLoadFormatNames() {
		return new String[0];
	}
	
	/*
	 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormatProvider#getSaveFormatNames()
	 */
	public String[] getSaveFormatNames() {
		StringVector formats = new StringVector();
		String[] dataNames = this.dataProvider.getDataNames();
		for (int n = 0; n < dataNames.length; n++) {
			if (dataNames[n].endsWith(".xslt") || dataNames[n].endsWith(".xsl"))
				formats.addElementIgnoreDuplicates(dataNames[n]);
		}
		return formats.toStringArray();
	}
	
	/*
	 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormatProvider#getFormatForName(java.lang.String)
	 */
	public DocumentFormat getFormatForName(String formatName) {
		Transformer transformer = this.getTransformer(formatName);
		if (transformer == null)
			return null;
		else return new XsltDynamicDocumentFormat(formatName, transformer);
	}
	
//	/* (non-Javadoc)
//	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractDocumentFormatProvider#getDataNamesForResource(java.lang.String)
//	 */
//	public String[] getDataNamesForResource(String name) {
//		String[] names = {name + "@" + this.getClass().getName()};
//		return names; // return the name of the stylesheet to copy
//	}
//	
	/*
	 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormatProvider#getLoadFileFilters()
	 */
	public DocumentFormat[] getLoadFileFilters() {
		return new DocumentFormat[0];
	}
	
	/*
	 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormatProvider#getSaveFileFilters()
	 */
	public DocumentFormat[] getSaveFileFilters() {
		DocumentFormat[] formats = {new XsltDynamicDocumentFormat()};
		return formats;
	}
	
	private Transformer getTransformer(String name) {
		if (name == null)
			return null;
		
		try {
			Transformer transformer = XsltUtils.getTransformer(("OutputFormat:" + name), this.dataProvider.getInputStream(name), true);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			return transformer;
		}
		catch (IOException ioe) {
			System.out.println(ioe.getClass().getName() + " (" + ioe.getMessage() + ") while obtaining Transformer.");
			ioe.printStackTrace(System.out);
		}
		
		return null;
	}
	
	private class XsltDynamicDocumentFormat extends DocumentFormat {
		
		private String name;
		private Transformer transformer = null;
		
		XsltDynamicDocumentFormat() {
			this(null, null);
		}
		
		XsltDynamicDocumentFormat(String name, Transformer transformer) {
			this.name = name;
			this.transformer = transformer;
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#getDefaultSaveFileExtension()
		 */
		public String getDefaultSaveFileExtension() {
			return "xml";
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#loadDocument(java.io.Reader)
		 */
		public MutableAnnotation loadDocument(Reader source) throws IOException {
			return null;
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#saveDocument(de.goldenGate.DocumentEditor, java.io.OutputStream)
		 */
		public boolean saveDocument(DocumentEditor data, OutputStream out) throws IOException {
			return this.saveDocument(data, this.getOutputStreamWriter(out, UTF_8_ENCODING_NAME));
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#saveDocument(de.uka.ipd.idaho.gamta.QueriableAnnotation, java.io.OutputStream)
		 */
		public boolean saveDocument(QueriableAnnotation data, OutputStream out) throws IOException {
			return this.saveDocument(data, this.getOutputStreamWriter(out, UTF_8_ENCODING_NAME));
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#saveDocument(de.goldenGate.DocumentEditor, de.uka.ipd.idaho.gamta.QueriableAnnotation, java.io.OutputStream)
		 */
		public boolean saveDocument(DocumentEditor data, QueriableAnnotation doc, OutputStream out) throws IOException {
			return this.saveDocument(data, doc, this.getOutputStreamWriter(out, UTF_8_ENCODING_NAME));
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#saveDocument(de.uka.ipd.idaho.gamta.QueriableAnnotation, java.io.Writer)
		 */
		public boolean saveDocument(QueriableAnnotation data, Writer out) throws IOException {
			return this.saveDocument(null, data, out);
		}
		
		/* 
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#saveDocument(de.goldenGate.DocumentEditor, java.io.Writer)
		 */
		public boolean saveDocument(DocumentEditor data, Writer out) throws IOException {
			return this.saveDocument(data, data.getContent(), out);
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#saveDocument(de.goldenGate.DocumentEditor, de.uka.ipd.idaho.gamta.MutableAnnotation, java.io.Writer)
		 */
		public boolean saveDocument(DocumentEditor data, QueriableAnnotation doc, Writer out) throws IOException {
			if (this.transformer == null) {
				ResourceDialog rd = ResourceDialog.getResourceDialog(getSaveFormatNames(), "Select XSLT Stylesheet", "Select");
				rd.setVisible(true);
				if (rd.isCommitted()) {
					this.name = rd.getSelectedResourceName();
					this.transformer = getTransformer(this.name);
				}
				else return false;
			}
			try {
				this.transformer.transform(new StreamSource(new AnnotationInputStream(doc, "  ", "utf-8")), new StreamResult(out));
				out.flush();
				return true;
			}
			catch (TransformerException e) {
				throw new IOException(e.getMessageAndLocation());
			}
		}
		
		/* 
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#accept(java.lang.String)
		 */
		public boolean accept(String fileName) {
			return fileName.toLowerCase().endsWith(".xml");
		}
		
		/*
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		public String getDescription() {
			return "XML file (XSLT " + ((this.name == null) ? "transformed" : this.name) + ")";
		}
		
		/*
		 * @see de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat#equals(de.uka.ipd.idaho.goldenGate.plugins.DocumentFormat)
		 */
		public boolean equals(DocumentFormat format) {
			if (format == null)
				return false;
			if (!(format instanceof XsltDynamicDocumentFormat))
				return false;
			XsltDynamicDocumentFormat dwdf = ((XsltDynamicDocumentFormat) format);
			if (this.name == null)
				return true; // allow an undetermined instance to be pre-selected if the previous selection was another instance that has been determined in the meantime 
			else return this.name.equals(dwdf.name);
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.goldenGate.plugins.Resource#getName()
		 */
		public String getName() {
			return this.name;
		}
		
		/* (non-Javadoc)
		 * @see de.uka.ipd.idaho.goldenGate.plugins.Resource#getProviderClassName()
		 */
		public String getProviderClassName() {
			return XsltOutputFormatter.class.getName();
		}
	}
}
