/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mousepilots.es.model;

import javax.persistence.metamodel.IdentifiableType;

/**
 *
 * @author clevenro
 */
public interface IdentifiableTypeES<T>  extends ManagedTypeES<T>,IdentifiableType<T>{
    
}
