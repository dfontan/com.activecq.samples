/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.activecq.samples.loginmodule;

import java.security.Principal;

/**
 *
 * @author david
 */
public class SamplePrincipal implements Principal {
    private String name;

    public SamplePrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUserId() {
        return name;
    }


}
