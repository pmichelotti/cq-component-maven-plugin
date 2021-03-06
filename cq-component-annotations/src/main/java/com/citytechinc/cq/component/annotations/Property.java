/**
 *    Copyright 2017 ICF Olson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.citytechinc.cq.component.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a property to be written to a dialog widget's XML node in the
 * Component's dialog.xml.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface Property {

	/**
	 * The name of the property on the field
	 *
	 * @return String
	 */
	String name();

	/**
	 * The value of the property on the field
	 *
	 * @return String
	 */
	String value();
	
	enum RenderValue {
		BOTH, CLASSIC, TOUCH
	}
	
	/**
	 * When used in DialogField.additionalProperties this field will determine 
	 * whether this property should be rendered for touch UI, classic UI, or both.
	 *
	 * @return RenderValue
	 */
	RenderValue renderIn() default RenderValue.BOTH;

}
