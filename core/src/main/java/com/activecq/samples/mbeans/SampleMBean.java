/*
 * Copyright 2013 david gonzalez.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.activecq.samples.mbeans;

import com.adobe.granite.jmx.annotation.Description;

/**
 * User: david
 */
@Description("Description of the MBean as displayed in the JMX Console")
public interface SampleMBean {

    /**
     * Attributes
     *
     * Attributes are defined by getter/setter naming conventions
     * **/

    public String getAttributeOne();

    // Setters optionally allow the value to be set via JMX
    public void setAttributeOne(String attributeOne);

    /**
     *
     * Operations
     *
     * Public methods not using the getter/setter naming conventions
     * **/

    @Description("Description of the operation as displays next to the operation in the JMX Console")
    public String helloWorld();
}
