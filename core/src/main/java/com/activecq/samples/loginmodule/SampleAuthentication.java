/*
 * Copyright 2012 david gonzalez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activecq.samples.loginmodule;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;


/**
 * @author david
 */
public class SampleAuthentication implements Authentication {
    private static final Logger log = LoggerFactory.getLogger(SampleAuthentication.class);

    @Override
    public boolean canHandle(Credentials credentials) {
        // Don't handle Credentials if they are null
        if (credentials == null) {
            return false;
        }

        // Only handle SimpleCredentials
        if (!(credentials instanceof SimpleCredentials)) {
            return false;
        }

        SimpleCredentials sc = (SimpleCredentials) credentials;

        return true;
    }

    @Override
    public boolean authenticate(Credentials credentials) throws RepositoryException {
        if (!(credentials instanceof SimpleCredentials)) {
            return false;
        }

        SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
        final String userId = simpleCredentials.getUserID();

        return StringUtils.equals(userId, "davidg");
    }
}
