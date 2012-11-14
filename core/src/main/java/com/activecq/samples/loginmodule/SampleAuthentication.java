/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.loginmodule;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.authentication.AbstractLoginModule;
import org.apache.jackrabbit.core.security.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author david
 */
public class SampleAuthentication implements Authentication {
    private static final Logger log = LoggerFactory.getLogger(SampleAuthentication.class);

    @Override
    public boolean canHandle(Credentials credentials) {
        // Don't handle Credentials if they are null
        if(credentials == null) { return false; }

        // Only handle SimpleCredentials
        if(!(credentials instanceof SimpleCredentials)) { return false; }

        SimpleCredentials sc = (SimpleCredentials) credentials;

        return true;
    }

    @Override
    public boolean authenticate(Credentials credentials) throws RepositoryException {
         if(!(credentials instanceof SimpleCredentials)) { return false; }

         SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
         final String userId = simpleCredentials.getUserID();

         return StringUtils.equals(userId, "davidg");
    }
}
