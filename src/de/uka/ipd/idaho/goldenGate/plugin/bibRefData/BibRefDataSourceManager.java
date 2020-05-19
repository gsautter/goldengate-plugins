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
 *     * Neither the name of the Universitaet Karlsruhe (TH) / KIT nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY UNIVERSITAET KARLSRUHE (TH) / KIT AND CONTRIBUTORS 
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
package de.uka.ipd.idaho.goldenGate.plugin.bibRefData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import de.uka.ipd.idaho.gamta.util.AbstractAnalyzerDataProvider;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin;
import de.uka.ipd.idaho.plugins.bibRefs.BibRefDataSource;

/**
 * Plugin centrally initializing bibliographic reference data sources.
 * 
 * @author sautter
 */
public class BibRefDataSourceManager extends AbstractGoldenGatePlugin {
	
	/** Zero argument constructor for class loading */
	public BibRefDataSourceManager() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#init()
	 */
	public void init() {
		BibRefDataSource.loadDataSources(new AbstractAnalyzerDataProvider() {
			/* facilitates holding all the settings in one file
			 * 
			 * need to refrain from doing that if we're getting data
			 * sources whose setting names incur collisions, though */
			private String checkDataName(String dataName) {
				if (dataName == null)
					return dataName;
				else if ((dataName.startsWith("config.") || (dataName.indexOf("/config.") != -1)) && dataName.endsWith(".cnfg"))
					return "config.cnfg";
				else return dataName;
			}
			public boolean deleteData(String dataName) {
				dataName = this.checkDataName(dataName);
				return dataProvider.deleteData(dataName);
			}
			public String[] getDataNames() {
				return dataProvider.getDataNames();
			}
			public InputStream getInputStream(String dataName) throws IOException {
				dataName = this.checkDataName(dataName);
				return dataProvider.getInputStream(dataName);
			}
			public OutputStream getOutputStream(String dataName) throws IOException {
				dataName = this.checkDataName(dataName);
				return dataProvider.getOutputStream(dataName);
			}
			public URL getURL(String dataName) throws IOException {
				dataName = this.checkDataName(dataName);
				return dataProvider.getURL(dataName);
			}
			public boolean isDataAvailable(String dataName) {
				dataName = this.checkDataName(dataName);
				return dataProvider.isDataAvailable(dataName);
			}
			public boolean isDataEditable() {
				return dataProvider.isDataEditable();
			}
			public boolean isDataEditable(String dataName) {
				dataName = this.checkDataName(dataName);
				return dataProvider.isDataEditable(dataName);
			}
			public String getAbsolutePath() {
				return dataProvider.getAbsolutePath();
			}
		});
	}
}
