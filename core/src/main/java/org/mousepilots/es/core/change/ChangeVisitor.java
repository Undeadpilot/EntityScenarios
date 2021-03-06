/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mousepilots.es.core.change;

import org.mousepilots.es.core.change.impl.Create;
import org.mousepilots.es.core.change.impl.Delete;
import org.mousepilots.es.core.change.impl.EmbeddableJavaUtilCollectionBasicAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableSingularBasicAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToEmbeddableSingularAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToIdentifiableJavaUtilCollectionAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToIdentifiableSingularAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableJavaUtilCollectionBasicAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableSingularBasicAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToEmbeddableSingularAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToIdentifiableJavaUtilCollectionAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToIdentifiableToIdentifiableJavaUtilMapAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToIdentifiableSingularAssociationAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToNonIdentifiableToIdentifiableJavaUtilMapAttributeUpdate;
import org.mousepilots.es.core.change.impl.IdentifiableToNonIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToIdentifiableToIdentifiableJavaUtilMapAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToNonIdentifiableToIdentifiableJavaUtilMapAttributeUpdate;
import org.mousepilots.es.core.change.impl.EmbeddableToNonIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate;

/**
 *
 * @author Roy Cleven
 */
public interface ChangeVisitor {

    /**
     * Visit which handles the creation change and checks if it is allowed.
     * @param create A create change holding the values needed for the creation of an entity
     */
    void visit(Create create);

    /**
     * Visit which handles the deletion change and checks if it is allowed.
     * @param delete A delete change holding the values needed for the deletion of an entity
     */
    void visit(Delete delete);

    /**
     * Handles the change which holds an embeddable as source and has the new
     * values for a list of basic's which is contained by the embeddable.
     * @param update a filled {@link EmbeddableJavaUtilCollectionBasicAttributeUpdate}
     */
    void visit(EmbeddableJavaUtilCollectionBasicAttributeUpdate update);

    /**
     * Handles the change which holds an embeddable and has the new values for 
     * a list of associations to different embeddables which is contained by the
     * embeddable.
     * @param update a filled {@link EmbeddableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate}
     */
    void visit(EmbeddableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an embeddable and
     * has the new value for an assocation to a different embeddable
     * which is contained by the embeddable.
     * @param update a filled {@link EmbeddableToEmbeddableSingularAssociationAttributeUpdate}
     */
    void visit(EmbeddableToEmbeddableSingularAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an embeddable and
     * has the new valeus for a list of assocations to multiple identifiable 
     * entities which is contained by the embeddable.
     * @param update a filled {@link EmbeddableToIdentifiableJavaUtilCollectionAssociationAttributeUpdate}
     */
    void visit(EmbeddableToIdentifiableJavaUtilCollectionAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an embeddable and
     * has the new value for an assocation to an identifiable entity which is
     * contained by the embeddable.
     * @param update a filled {@link EmbeddableToIdentifiableSingularAssociationAttributeUpdate}
     */
    void visit(EmbeddableToIdentifiableSingularAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an embeddable and
     * the new value for a basic which is contained by the embeddable.
     * @param update a filled {@link EmbeddableSingularBasicAttributeUpdate}
     */
    void visit(EmbeddableSingularBasicAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new values for
     * a list of basic's which is contained by the identifiable.
     * @param update a filled {@link IdentifiableJavaUtilCollectionBasicAttributeUpdate}
     */
    void visit(IdentifiableJavaUtilCollectionBasicAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new value for
     * a basic which is contained by the identifiable.
     * @param update a filled {@link IdentifiableSingularBasicAttributeUpdate}
     */
    void visit(IdentifiableSingularBasicAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new value for an
     * embeddable which is contained by the identifiable.
     * @param update a filled {@link IdentifiableToEmbeddableSingularAssociationAttributeUpdate}
     */
    void visit(IdentifiableToEmbeddableSingularAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new value for 
     * an assocation to an identifiable entity which is contained by the 
     * identifiable.
     * @param update a filled {@link IdentifiableToIdentifiableSingularAssociationAttributeUpdate}
     */
    void visit(IdentifiableToIdentifiableSingularAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new values
     * for a list of associations to multiple embeddables which is
     * contained by the identifiable.
     * @param update a filled {@link IdentifiableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate}
     */
    void visit(IdentifiableToEmbeddableJavaUtilCollectionAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new values for
     * a list of associations to multiple identifiable entities which is
     * contained by the identifiable.
     * @param update a filled {@link IdentifiableToIdentifiableJavaUtilCollectionAssociationAttributeUpdate}
     */
    void visit(IdentifiableToIdentifiableJavaUtilCollectionAssociationAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new values for
     * a map which has an assosiaction to an identifiable entity as key and 
     * an association to an identifiable entity
     * as value.
     * @param update a filled {@link IdentifiableToIdentifiableToIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(IdentifiableToIdentifiableToIdentifiableJavaUtilMapAttributeUpdate update);

    /**
     * handles the change which holds an identifiable and the new values for
     * a map which has an assocation to an identifiable entity as key and a 
     * non-identifiable as value.
     * @param update a filled {@link IdentifiableToIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(IdentifiableToIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new values for
     * a map which has a non-identifiable as key and an association to an 
     * identifiable entity as value
     * @param update a filled {@link IdentifiableToNonIdentifiableToIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(IdentifiableToNonIdentifiableToIdentifiableJavaUtilMapAttributeUpdate update);

    /**
     * Handles the change which holds an identifiable and the new values for
     * a map which has a non-identifiable as key and a non-identifiable as value
     * @param update a filled {@link IdentifiableToNonIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(IdentifiableToNonIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate update);

    /**
     * Handles the change which holds an embeddable and the new values for
     * a map which has an association to an identifiable entity as key and
     * an association to an identifiably entity as key.
     * @param update a filled {@link EmbeddableToIdentifiableToIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(EmbeddableToIdentifiableToIdentifiableJavaUtilMapAttributeUpdate update);

    /**
     * handles the change which holds an embeddable and the new values for 
     * a map which has an association to an identifiable entity as key and
     * a non-identifiable as value.
     * @param update a filled {@link EmbeddableToIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(EmbeddableToIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate update);

    /**
     * Handles the change which holds an embeddable and the new values for
     * a map which has an association to a non-identifiable as key and
     * an association to an identifiable entity as value.
     * @param update a filled {@link EmbeddableToNonIdentifiableToIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(EmbeddableToNonIdentifiableToIdentifiableJavaUtilMapAttributeUpdate update);
    
    /**
     * Handles the change which holds an embeddable and the new values for
     * a map which has a non-identifiable as key and a non-identifiable as value
     * @param update a filled {@link EmbeddableToNonIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate}
     */
    void visit(EmbeddableToNonIdentifiableToNonIdentifiableJavaUtilMapAttributeUpdate update);
    
    /**
     * Gets an {@link ExecutionSummary} based on all the changes which were visited.
     * @return a filled {@link ExecutionSummary}
     */
    ExecutionSummary getExecutionSummary();
}
