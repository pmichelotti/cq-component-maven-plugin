package com.citytechinc.cq.component.dialog.impl;

import java.util.Map;

import com.citytechinc.cq.component.annotations.config.Widget;
import com.citytechinc.cq.component.annotations.widgets.NumberField;
import com.citytechinc.cq.component.dialog.AbstractWidget;
import com.citytechinc.cq.component.dialog.maker.impl.NumberFieldMaker;

@Widget(annotationClass = NumberField.class, makerClass = NumberFieldMaker.class, xtypes = NumberFieldWidget.XTYPE)
public class NumberFieldWidget extends AbstractWidget {
	public static final String XTYPE = "numberfield";
	private static final String PRIMARY_TYPE = "cq:Widget";

	private final boolean allowDecimals;
	private final boolean allowNegative;
	private final int decimalPrecision;
	private final String decimalSeparator;

	public NumberFieldWidget(boolean allowDecimals, boolean allowNegative, int decimalPrecision,
		String decimalSeparator, String fieldLabel, String fieldDescription, boolean allowBlank, boolean hideLabel,
		String defaultValue, String name, String fieldName, Map<String, String> additionalProperties) {
		super(XTYPE, fieldLabel, fieldDescription, allowBlank, hideLabel, defaultValue, name, PRIMARY_TYPE, null,
			fieldName, additionalProperties, null);
		this.allowDecimals = allowDecimals;
		this.allowNegative = allowNegative;
		this.decimalPrecision = decimalPrecision;
		this.decimalSeparator = decimalSeparator;
	}

	public boolean isAllowDecimals() {
		return allowDecimals;
	}

	public boolean isAllowNegative() {
		return allowNegative;
	}

	public int getDecimalPrecision() {
		return decimalPrecision;
	}

	public String getDecimalSeparator() {
		return decimalSeparator;
	}

}
