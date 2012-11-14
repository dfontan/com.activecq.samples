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

import java.security.Principal;
import java.util.Properties;
import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.api.security.principal.PrincipalIterator;
import org.apache.jackrabbit.core.security.principal.PrincipalProvider;

/**
 *
 * @author david
 */
public class SamplePrincipalProvider implements PrincipalProvider {

    @Override
    public Principal getPrincipal(String string) {
        if("davidg".contains(string)) {
            return new SamplePrincipal("davidg");
        }

        return null;
    }

    /**
     *
     * @param credentials
     * @return
     */
    private boolean canProvidePrincipal(Credentials credentials) {
        if(!(credentials instanceof SimpleCredentials)) {
            return false;
        }

        SimpleCredentials sc = (SimpleCredentials) credentials;

        return StringUtils.equals("davidg", sc.getUserID());
    }

    @Override
    public PrincipalIterator findPrincipals(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PrincipalIterator findPrincipals(String string, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PrincipalIterator getPrincipals(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PrincipalIterator getGroupMembership(Principal prncpl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(Properties prprts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canReadPrincipal(Session sn, Principal prncpl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
