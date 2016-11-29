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
 *     * Neither the name of the Universität Karlsruhe (TH) / KIT nor the
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
package de.uka.ipd.idaho.goldenGate.plugin.attributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

import de.uka.ipd.idaho.easyIO.settings.Settings;
import de.uka.ipd.idaho.gamta.Annotation;
import de.uka.ipd.idaho.gamta.Attributed;
import de.uka.ipd.idaho.gamta.util.swing.AttributeEditor;
import de.uka.ipd.idaho.gamta.util.swing.AttributeEditor.AttributeValueList;
import de.uka.ipd.idaho.gamta.util.swing.AttributeEditor.AttributeValueProvider;
import de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager;

/**
 * Provider of attribute and attribute value suggestions.
 * 
 * @author sautter
 */
public class AttributeSuggestionManager extends AbstractResourceManager implements AttributeValueProvider {
	
	/** zero-argument constructor for class loading */
	public AttributeSuggestionManager() {}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#init()
	 */
	public void init() {
		AttributeEditor.addAttributeValueProvider(this);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractGoldenGatePlugin#exit()
	 */
	public void exit() {
		AttributeEditor.removeAttributeValueProvider(this);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getPluginName()
	 */
	public String getPluginName() {
		return "Attribute Suggestion Manager";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.ResourceManager#getResourceTypeLabel()
	 */
	public String getResourceTypeLabel() {
		return "Attribute Suggestion Set";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.goldenGate.plugins.AbstractResourceManager#getFileExtension()
	 */
	protected String getFileExtension() {
		return ".attributeSuggestions";
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.swing.AttributeEditor.AttributeValueProvider#getAttributesFor(de.uka.ipd.idaho.gamta.Attributed)
	 */
	public String[] getAttributesFor(Attributed attributed) {
		return ((attributed instanceof Annotation) ? this.getAttributesFor(((Annotation) attributed).getType()) : null);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.swing.AttributeEditor.AttributeValueProvider#getAttributesFor(java.lang.String)
	 */
	public String[] getAttributesFor(String type) {
		
		//	try cache first (contains all type we attempted to load attributes for, also where we didn't find any)
		if (this.attributeNameSuggestions.containsKey(type))
			return ((String[]) this.attributeNameSuggestions.get(type));
		
		//	load data from provider and cache result
		String[] attributeNames = this.loadAttributesFor(type);
		this.attributeNameSuggestions.put(type, attributeNames);
		return attributeNames;
	}
	private HashMap attributeNameSuggestions = new HashMap();
	private String[] loadAttributesFor(String type) {
		Settings set = this.loadSettingsResource(type + this.getFileExtension());
		if (set == null)
			return null;
		String attributeNamesStr = set.getSetting("a_t_t_r_i_b_u_t_e_s");
		if (attributeNamesStr == null)
			return null;
		LinkedHashSet attributeNamesSet = new LinkedHashSet(Arrays.asList(attributeNamesStr.split("\\s*\\|\\s*")));
		attributeNamesSet.remove("");
		return (attributeNamesSet.isEmpty() ? null : ((String[]) attributeNamesSet.toArray(new String[attributeNamesSet.size()])));
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.swing.AttributeEditor.AttributeValueProvider#getValuesFor(de.uka.ipd.idaho.gamta.Attributed, java.lang.String)
	 */
	public AttributeValueList getValuesFor(Attributed attributed, String attributeName) {
		return ((attributed instanceof Annotation) ? this.getValuesFor(((Annotation) attributed).getType(), attributeName) : null);
	}
	
	/* (non-Javadoc)
	 * @see de.uka.ipd.idaho.gamta.util.swing.AttributeEditor.AttributeValueProvider#getValuesFor(java.lang.String, java.lang.String)
	 */
	public AttributeValueList getValuesFor(String type, String attributeName) {
		
		//	try cache first (contains all type we attempted to load attributes for, also where we didn't find any)
		if (this.attributeValueSuggestions.containsKey(type + "." + attributeName))
			return ((AttributeValueList) this.attributeNameSuggestions.get(type + "." + attributeName));
		
		//	load data from provider and cache result
		AttributeValueList attributeValues = this.loadValuesFor(type, attributeName);
		this.attributeValueSuggestions.put((type + "." + attributeName), attributeValues);
		return attributeValues;
	}
	private HashMap attributeValueSuggestions = new HashMap();
	private AttributeValueList loadValuesFor(String type, String attributeName) {
		Settings set = this.loadSettingsResource(type + this.getFileExtension());
		if (set == null)
			return null;
		String attributeValuesStr = set.getSetting(attributeName);
		if (attributeValuesStr == null)
			return null;
		LinkedHashSet attributeValuesSet = new LinkedHashSet(Arrays.asList(attributeValuesStr.split("\\s*\\|\\s*")));
		attributeValuesSet.remove("");
		return (attributeValuesSet.isEmpty() ? null : new AttributeValueList(attributeValuesSet, false));
	}
	
	//	TODO add editing facilities ... at some point
}