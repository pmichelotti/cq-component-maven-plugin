package com.citytechinc.cq.component.dialog.factory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import org.codehaus.plexus.util.StringUtils;

import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.annotations.FieldConfig;
import com.citytechinc.cq.component.annotations.FieldProperty;
import com.citytechinc.cq.component.dialog.MultiValueWidget;
import com.citytechinc.cq.component.dialog.Option;
import com.citytechinc.cq.component.dialog.SelectionWidget;
import com.citytechinc.cq.component.dialog.Widget;
import com.citytechinc.cq.component.dialog.exception.InvalidComponentFieldException;
import com.citytechinc.cq.component.dialog.impl.BasicFieldConfig;
import com.citytechinc.cq.component.dialog.impl.SimpleMultiValueWidget;
import com.citytechinc.cq.component.dialog.impl.SimpleOption;
import com.citytechinc.cq.component.dialog.impl.SimpleSelectionWidget;
import com.citytechinc.cq.component.dialog.impl.SimpleWidget;

public class WidgetFactory {

	public static final String TEXTFIELD_XTYPE = "textfield";
	public static final String NUMBERFIELD_XTYPE = "numberfield";
	public static final String PATHFIELD_XTYPE = "pathfield";
	public static final String SELECTION_XTYPE = "selection";
	public static final String MULTIFIELD_XTYPE = "multifield";

	public static Widget make(CtClass componentClass, CtField annotatedWidgetField, Field widgetField, Map<Class<?>, String> xtypeMap, ClassLoader classLoader, ClassPool classPool)
			throws InvalidComponentFieldException, ClassNotFoundException, CannotCompileException, NotFoundException {

		DialogField propertyAnnotation = (DialogField) annotatedWidgetField.getAnnotation(DialogField.class);

		if (propertyAnnotation == null) {
			throw new InvalidComponentFieldException();
		}

		String xtype = getXTypeForField(widgetField, propertyAnnotation, xtypeMap, classLoader);
		String name = getNameForField(annotatedWidgetField, propertyAnnotation);
		String fieldName = getFieldNameForField(annotatedWidgetField, propertyAnnotation);
		String fieldLabel = getFieldLabelForField(annotatedWidgetField, propertyAnnotation);
		String fieldDescription = getFieldDescriptionForField(annotatedWidgetField, propertyAnnotation);
		Boolean isRequired = getIsRequiredPropertyForField(annotatedWidgetField, propertyAnnotation);
		Map<String, String> additionalProperties = getAdditionalPropertiesForField(annotatedWidgetField, propertyAnnotation);

		if (xtype.equals(SELECTION_XTYPE)) {

			return buildSelectionWidget(
					componentClass,
					annotatedWidgetField,
					propertyAnnotation,
					name,
					fieldName,
					fieldLabel,
					fieldDescription,
					isRequired,
					additionalProperties,
					classLoader,
					classPool);

		}

		if (xtype.equals(MULTIFIELD_XTYPE)) {

			return buildMultiFieldWidget(
					componentClass,
					widgetField,
					propertyAnnotation,
					name,
					fieldName,
					fieldLabel,
					fieldDescription,
					isRequired,
					additionalProperties,
					xtypeMap);
		}

		return new SimpleWidget(xtype, name, fieldName, fieldLabel, fieldDescription, isRequired, additionalProperties);

	}

	private static final Map<String, String> getAdditionalPropertiesForField(CtField widgetField, DialogField propertyAnnotation) {

		if (propertyAnnotation.additionalProperties().length > 0) {
			Map<String, String> properties = new HashMap<String, String>();

			for (FieldProperty curProperty : propertyAnnotation.additionalProperties()) {
				properties.put(curProperty.name(), curProperty.value());
			}

			return properties;
		}

		return null;

	}

	private static final Boolean getIsRequiredPropertyForField(CtField widgetField, DialogField propertyAnnotation) {
		return propertyAnnotation.required() ==true;
	}

	private static final String getNameForField(CtField widgetField, DialogField propertyAnnotation) {

		String overrideName = propertyAnnotation.name();

		if (StringUtils.isNotEmpty(overrideName)) {
			return overrideName;
		}

		return "./" + widgetField.getName();
	}

	private static final String getFieldLabelForField(CtField widgetField, DialogField propertyAnnotation) {

		String overrideLabel = propertyAnnotation.fieldLabel();

		if (StringUtils.isNotEmpty(overrideLabel)) {
			return overrideLabel;
		}

		return widgetField.getName();
	}

	private static final String getFieldNameForField(CtField widgetField, DialogField propertyAnnotation) {

		String overrideFieldName = propertyAnnotation.fieldName();

		if (StringUtils.isNotEmpty(overrideFieldName)) {
			return overrideFieldName;
		}

		return widgetField.getName();
	}

	private static final String getFieldDescriptionForField(CtField widgetField, DialogField propertyAnnotation) {

		String overrideFieldDescription = propertyAnnotation.fieldDescription();

		if (StringUtils.isNotEmpty(overrideFieldDescription)) {
			return overrideFieldDescription;
		}

		return null;
	}

	private static final MultiValueWidget buildMultiFieldWidget(
			CtClass componentClass,
			Field widgetField,
			DialogField fieldAnnotation,
			String name,
			String fieldName,
			String fieldLabel,
			String fieldDescription,
			Boolean isRequired,
			Map<String, String> additionalProperties,
			Map<Class<?>, String> xtypeMap) throws InvalidComponentFieldException {

		String innerXType = getInnerXTypeForMultiField(widgetField, fieldAnnotation, xtypeMap);

		if (innerXType == null) {
			throw new InvalidComponentFieldException("Invalid or unsupported field annotation on a multi valued field");
		}

		Widget fieldConfig = new BasicFieldConfig(innerXType);

		List<Widget> fieldConfigs = new ArrayList<Widget>();

		fieldConfigs.add(fieldConfig);

		return new SimpleMultiValueWidget(MULTIFIELD_XTYPE, name, fieldName, fieldLabel, fieldDescription, isRequired, additionalProperties, fieldConfigs);
	}

	private static final String getInnerXTypeForMultiField(Field widgetField, DialogField fieldAnnotation, Map<Class<?>, String> xtypeMap) throws InvalidComponentFieldException {

		List<FieldConfig> fieldConfigs = Arrays.asList(fieldAnnotation.fieldConfigs());

		/*
		 * Check to see if a field configuration annotation is present.  If so, use that
		 */
		if (!fieldConfigs.isEmpty()) {
			return fieldConfigs.get(0).xtype();
		}

		/*
		 * Otherwise, see if we can derive the xtype from the parameterized field
		 */
		String innerXType = getInnerXTypeForField(widgetField, xtypeMap);

		if (innerXType != null) {
			return innerXType;
		}

		return null;
	}

	private static final SelectionWidget buildSelectionWidget(
			CtClass componentClass,
			CtField widgetField,
			DialogField fieldAnnotation,
			String name,
			String fieldName,
			String fieldLabel,
			String fieldDescription,
			Boolean isRequired,
			Map<String, String> additionalProperties,
			ClassLoader classLoader,
			ClassPool classPool) throws InvalidComponentFieldException, CannotCompileException, NotFoundException, ClassNotFoundException {

		List<Option> options = buildSelectionOptionsForField(widgetField, fieldAnnotation, classLoader, classPool);

		String selectionType = getSelectionTypeForField(widgetField, fieldAnnotation);

		return new SimpleSelectionWidget(selectionType, name, fieldLabel, fieldName, fieldDescription, isRequired, additionalProperties, options);

	}

	private static final String getSelectionTypeForField(CtField widgetField, DialogField fieldAnnotation) {
		return fieldAnnotation.selectionType().name().toLowerCase();
	}

	private static final List<Option> buildSelectionOptionsForField(CtField widgetField, DialogField fieldAnnotation, ClassLoader classLoader, ClassPool classPool) throws InvalidComponentFieldException, CannotCompileException, NotFoundException, ClassNotFoundException {

		List<Option> options = new ArrayList<Option>();

		/*
		 * Options specified in the annotation take precedence
		 */
		if (fieldAnnotation.selectionOptions().length > 0) {
			for(com.citytechinc.cq.component.annotations.Option curOptionAnnotation : fieldAnnotation.selectionOptions()) {
				if (StringUtils.isEmpty(curOptionAnnotation.text()) || StringUtils.isEmpty(curOptionAnnotation.value())) {
					throw new InvalidComponentFieldException("Selection Options specified in the selectionOptions Annotation property must include a non-empty text and value attribute");
				}
				options.add(new SimpleOption(curOptionAnnotation.text(), curOptionAnnotation.value()));
			}
		}
		/*
		 * If options were not specified by the annotation then we check
		 * to see if the field is an Enum and if so, the options are pulled
		 * from the Enum definition
		 */
		else if (widgetField.getType().isEnum()) {
			for(Object curEnumObject : classLoader.loadClass(widgetField.getType().getName()).getEnumConstants()) {
				Enum<?> curEnum = (Enum<?>) curEnumObject;
				try {
					options.add(buildSelectionOptionForEnum(curEnum, classPool));
				} catch (SecurityException e) {
					throw new InvalidComponentFieldException("Invalid Enum Field", e);
				} catch (NoSuchFieldException e) {
					throw new InvalidComponentFieldException("Invalid Enum Field", e);
				}
			}
		}

		return options;

	}

	//TODO: This isn't going to work
	private static final Option buildSelectionOptionForEnum(Enum<?> optionEnum, ClassPool classPool)
			throws SecurityException, NoSuchFieldException, NotFoundException, ClassNotFoundException {

		String text = optionEnum.name();
		String value = optionEnum.name();

		CtClass annotatedEnumClass = classPool.getCtClass(optionEnum.getDeclaringClass().getName());
		CtField annotatedEnumField = annotatedEnumClass.getField(optionEnum.name());
		com.citytechinc.cq.component.annotations.Option optionAnnotation = (com.citytechinc.cq.component.annotations.Option) annotatedEnumField.getAnnotation(com.citytechinc.cq.component.annotations.Option.class);

		if (optionAnnotation != null) {
			if (StringUtils.isNotEmpty(optionAnnotation.text())) {
				text = optionAnnotation.text();
			}
			if (StringUtils.isNotEmpty(optionAnnotation.value())) {
				value = optionAnnotation.value();
			}
		}

		return new SimpleOption(text, value);

	}

	private static final String getXTypeForField(Field widgetField, DialogField propertyAnnotation, Map<Class<?>, String> xtypeMap, ClassLoader classLoader) throws InvalidComponentFieldException, CannotCompileException, NotFoundException, ClassNotFoundException {

		/*
		 * Handle annotated xtypes
		 */
		String overrideXType = propertyAnnotation.xtype();

		if (StringUtils.isNotEmpty(overrideXType)) {
			return overrideXType;
		}

		Class<?> fieldClass = widgetField.getType();

		/*
		 * Handle custom types
		 */
		for (Class<?> curCustomClass : xtypeMap.keySet()) {
			if (curCustomClass.isAssignableFrom(fieldClass)) {
				return xtypeMap.get(curCustomClass);
			}
		}

		/*
		 * Handle standard types
		 */

		/*
		 * numberfield
		 */
		if (
				Number.class.isAssignableFrom(fieldClass) ||
				fieldClass.equals(int.class) ||
				fieldClass.equals(double.class) ||
				fieldClass.equals(float.class)) {
			return NUMBERFIELD_XTYPE;
		}

		/*
		 * textfield
		 */
		if (fieldClass.equals(String.class)) {
			return TEXTFIELD_XTYPE;
		}

		/*
		 *  pathfield
		 */
		if (URI.class.isAssignableFrom(fieldClass) || URL.class.isAssignableFrom(fieldClass)) {
			return PATHFIELD_XTYPE;
		}

		/*
		 * selection
		 */
		if (fieldClass.isEnum()) {
			return SELECTION_XTYPE;
		}

		if (List.class.isAssignableFrom(fieldClass) || fieldClass.isArray()) {

			String simpleXtype = getInnerXTypeForField(widgetField, xtypeMap);

			/*
			 * TODO: This is where the multicompositefield would end up being selected once implemented
			 */
			if (simpleXtype == null) {
				throw new InvalidComponentFieldException("Parameterized class for List is not of a supported type.  Currently supported types are numbers, strings, and links");
			}

			return MULTIFIELD_XTYPE;

		}

		/*
		 * If we could not determine an xtype, return textfield
		 *
		 * TODO: Determine if this is appropriate or if this should throw an Exception
		 */
		return TEXTFIELD_XTYPE;
	}

	private static final String getInnerXTypeForField(Field widgetField, Map<Class<?>, String> xtypeMap) throws InvalidComponentFieldException {

		Class<?> fieldClass = widgetField.getType();

		if (List.class.isAssignableFrom(fieldClass)) {
			return getInnerXTypeForListField(widgetField, xtypeMap);
		}
		if (fieldClass.isArray()) {
			return getInnerXTypeForArrayField(widgetField, xtypeMap);
		}

		throw new InvalidComponentFieldException("List dialog property found with a paramaterized type count not equal to 1");

	}

	private static final String getInnerXTypeForListField(Field widgetField, Map<Class<?>, String> xtypeMap) throws InvalidComponentFieldException {
		ParameterizedType parameterizedType = (ParameterizedType) widgetField.getGenericType();

		if (parameterizedType.getActualTypeArguments().length == 0 || parameterizedType.getActualTypeArguments().length > 1) {
			throw new InvalidComponentFieldException("List dialog property found with a paramaterized type count not equal to 1");
		}

		String simpleXtype = getSimpleXTypeForClass((Class<?>) parameterizedType.getActualTypeArguments()[0], xtypeMap);

		return simpleXtype;
	}

	private static final String getInnerXTypeForArrayField(Field widgetField, Map<Class<?>, String> xtypeMap) {
		Class<?> fieldClass = widgetField.getType();

		return getSimpleXTypeForClass(fieldClass.getComponentType(), xtypeMap);
	}

	private static final String getSimpleXTypeForClass(Class<?> fieldClass, Map<Class<?>, String> xtypeMap) {

		/*
		 * Handle custom types
		 */
		for (Class<?> curCustomClass : xtypeMap.keySet()) {
			if (curCustomClass.isAssignableFrom(fieldClass)) {
				return xtypeMap.get(curCustomClass);
			}
		}

		/*
		 * numberfield
		 */
		if (
				Number.class.isAssignableFrom(fieldClass) ||
				fieldClass.equals(int.class) ||
				fieldClass.equals(double.class) ||
				fieldClass.equals(float.class)) {
			return NUMBERFIELD_XTYPE;
		}

		/*
		 * textfield
		 */
		if (fieldClass.equals(String.class)) {
			return TEXTFIELD_XTYPE;
		}

		/*
		 *  pathfield
		 */
		if (URI.class.isAssignableFrom(fieldClass) || URL.class.isAssignableFrom(fieldClass)) {
			return PATHFIELD_XTYPE;
		}

		return null;

	}
}