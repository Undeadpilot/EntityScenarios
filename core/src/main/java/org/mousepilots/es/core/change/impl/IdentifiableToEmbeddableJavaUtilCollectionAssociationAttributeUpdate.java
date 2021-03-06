package org.mousepilots.es.core.change.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.mousepilots.es.core.change.ChangeVisitor;
import org.mousepilots.es.core.model.AttributeES;
import org.mousepilots.es.core.model.DtoType;
import org.mousepilots.es.core.model.HasValue;
import org.mousepilots.es.core.model.IdentifiableTypeES;

/**
 * @author Roy Cleven
 * @param <I>
 * @param <V>
 * @param <A>
 */
public final class IdentifiableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate<I extends Serializable, V extends Serializable, A extends Serializable> extends IdentifiableJavaUtilCollectionAssociationAttributeUpdate<I, V, A> {

    public IdentifiableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate() {
    }

    public IdentifiableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate(AttributeES attribute, V version, HasValue id, IdentifiableTypeES type, DtoType dtoType, Collection<Serializable> additions, Collection<Serializable> removals) {
        super(attribute, version, id, type, dtoType, additions, removals);
    }

    @Override
    public void accept(ChangeVisitor changeHandler) {
        changeHandler.visit(this);
    }
}