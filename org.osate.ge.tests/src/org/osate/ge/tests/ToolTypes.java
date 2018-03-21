package org.osate.ge.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.osate.aadl2.AbstractFeature;
import org.osate.aadl2.AbstractImplementation;
import org.osate.aadl2.AbstractSubcomponent;
import org.osate.aadl2.AbstractType;
import org.osate.aadl2.BusAccess;
import org.osate.aadl2.BusSubcomponent;
import org.osate.aadl2.BusType;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.DeviceSubcomponent;
import org.osate.aadl2.DeviceType;
import org.osate.aadl2.FeatureConnection;
import org.osate.aadl2.FeatureGroup;
import org.osate.aadl2.FeatureGroupType;
import org.osate.aadl2.Mode;
import org.osate.aadl2.ModeTransition;
import org.osate.aadl2.ProcessType;
import org.osate.aadl2.ProcessorSubcomponent;
import org.osate.aadl2.ProcessorType;
import org.osate.aadl2.SystemImplementation;
import org.osate.aadl2.SystemSubcomponent;

public class ToolTypes {
	private final static Map<Class<?>, String> toolTypes = createToolTypes();

	private static Map<Class<?>, String> createToolTypes() {
		final Map<Class<?>, String> map = new HashMap<Class<?>, String>();

		// Classifiers
		map.put(AbstractType.class, "Abstract Type");
		map.put(AbstractImplementation.class, "Abstract Implementation");
		map.put(FeatureGroupType.class, "Feature Group Type");
		map.put(ProcessorType.class, "Processor Type");
		map.put(ProcessType.class, "Process Type");
		map.put(SystemImplementation.class, "System Implementation");
		map.put(DeviceType.class, "Device Type");
		map.put(BusType.class, "Bus Type");

		// Features
		map.put(AbstractFeature.class, "Abstract Feature");
		map.put(FeatureGroup.class, "Feature Group");
		map.put(DataPort.class, "Data Port");
		map.put(BusAccess.class, "Bus Access");

		// Modes
		map.put(Mode.class, "Mode");
		map.put(ModeTransition.class, "Mode Transition");

		// Subcomponents
		map.put(AbstractSubcomponent.class, "Abstract Subcomponent");
		map.put(BusSubcomponent.class, "Bus Subcomponent");
		map.put(DeviceSubcomponent.class, "Device Subcomponent");
		map.put(SystemSubcomponent.class, "System Subcomponent");
		map.put(ProcessorSubcomponent.class, "Processor Subcomponent");

		// Connections
		map.put(FeatureConnection.class, "Feature Connection");

		return map;
	}

	public static String getToolItem(final Class<?> clazz) {
		return Objects.requireNonNull(toolTypes.get(clazz), "Unsupported tool item: " + clazz);
	}
}
