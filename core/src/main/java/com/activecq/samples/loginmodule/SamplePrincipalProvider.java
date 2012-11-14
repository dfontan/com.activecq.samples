/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
