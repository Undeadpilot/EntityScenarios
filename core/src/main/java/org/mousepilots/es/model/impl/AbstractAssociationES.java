package org.mousepilots.es.model.impl;

import java.util.Objects;
import org.mousepilots.es.model.AssociationTypeES;
import org.mousepilots.es.model.AssociationES;
import org.mousepilots.es.model.AttributeES;

/**
 * @author Nicky Ernste
 * @version 1.0, 3-11-2015
 */
public abstract class AbstractAssociationES implements AssociationES {
    
    private final AttributeES sourceAttribute;
    
    public AbstractAssociationES(AttributeES sourceAttribute){
        if (sourceAttribute == null){
            throw new IllegalArgumentException("The source attribute cannot be null.");
        }
        this.sourceAttribute = sourceAttribute;
    }

    @Override
    public AssociationTypeES getAssociationType() {
        return sourceAttribute.isAssociation(AssociationTypeES.KEY) ? AssociationTypeES.KEY : AssociationTypeES.VALUE;
    }

    @Override
    public AttributeES getSourceAttribute() {
        return sourceAttribute;
    }

    @Override
    public AssociationES getInverse() {
        //TODO use information for the annotation processor to determine the inverse relationship.
        return null;
    }

    @Override
    public boolean isOwner() {
        //TODO use information for the annotation processor to determine the owner of the relationship.
        return false;
    }

    @Override
    public boolean isBiDirectional() {
        //TODO use information for the annotation processor to determine if the relationship is bidirectional.
        return false;
    }    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.sourceAttribute);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractAssociationES other = (AbstractAssociationES) obj;
        if (!Objects.equals(this.sourceAttribute, other.sourceAttribute)) {
            return false;
        }
        return true;
    }
}