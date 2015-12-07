package org.mousepilots.es.server;

import org.mousepilots.es.core.change.ExecutionSummary;
import java.util.List;
import javax.persistence.EntityManager;
import org.mousepilots.es.core.change.Change;
import org.mousepilots.es.core.change.ChangeVisitor;
import org.mousepilots.es.core.change.exception.IllegalChangeException;
import org.mousepilots.es.core.change.exception.Reason;
import org.mousepilots.es.core.model.DtoType;
import org.mousepilots.es.core.model.MetaModelES;

/**
 * @author Roy Cleven
 */
public abstract class ChangeExecutor {
    
    protected abstract EntityManager getEntityManager();
    
    protected abstract MetaModelES getMetaModelES();
    
    
    
    public ExecutionSummary execute(List<Change> changes) throws IllegalChangeException{
        ChangeVisitor visitor= null;
        final Change changeForDtoCheck = changes.get(0);
        DtoType selectedDtoType = null;
        switch(changeForDtoCheck.getDtoType()){
            case SINGLE:
                selectedDtoType = DtoType.SINGLE;
                break;
            case MANAGED_CLASS:
                selectedDtoType = DtoType.MANAGED_CLASS;
                visitor = new ServerChangeVisitor(getEntityManager(), getMetaModelES());
                break;
            case MANAGED_SUB_CLASS:
                selectedDtoType = DtoType.MANAGED_SUB_CLASS;
                break;
        }
        if(visitor == null){
            throw new IllegalChangeException(changeForDtoCheck,Reason.DTO_TYPE_NOT_SUPPORTED);
        }
        for (Change change : changes) {
            if(change.getDtoType() != selectedDtoType){
                throw new IllegalChangeException(change,Reason.NOT_MATCHING_DTO_TYPE);
            }
            change.accept(visitor);
        }
        return null;
    }

}
