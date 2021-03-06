package org.mousepilots.es.maven.model.generator.controller;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import org.apache.maven.plugin.logging.Log;
import org.mousepilots.es.maven.model.generator.model.attribute.AttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.CollectionAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.ListAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.MapAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.SetAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.SingularAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.BasicTypeDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.EmbeddableTypeDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.EntityTypeDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.ManagedTypeDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.MappedSuperClassDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.TypeDescriptor;

/**
 * Class that generates all the {@link TypeDescriptor}s from the supplied meta
 * models.
 *
 * @author Nicky Ernste
 * @version 1.0, 19-11-2015
 */
public class TypeGenerator {

    private final AtomicInteger ordinal = new AtomicInteger(0);
    private final Log log;

    private static final Comparator<Class> CLASS_COMPARATOR = new Comparator<Class>() {

        @Override
        public int compare(Class o1, Class o2) {
            return o1.getCanonicalName().compareTo(o2.getCanonicalName());
        }
    };

    private static final Map<Class<? extends PluralAttribute>, Class> PLURAL_ATTRIBUTE_TO_COLLECTION_CLASS;

    static {
        Map<Class<? extends PluralAttribute>, Class> pluralAttributeToCollectionClass = new HashMap<>();
        pluralAttributeToCollectionClass.put(CollectionAttribute.class, Collection.class);
        pluralAttributeToCollectionClass.put(ListAttribute.class, List.class);
        pluralAttributeToCollectionClass.put(SetAttribute.class, Set.class);
        pluralAttributeToCollectionClass.put(MapAttribute.class, Map.class);
        PLURAL_ATTRIBUTE_TO_COLLECTION_CLASS = Collections.unmodifiableMap(pluralAttributeToCollectionClass);
    }

    public TypeGenerator(Log log) {
        this.log = log;
    }

    /**
     * Generate the {@link TypeDescriptor}s from the specified meta model
     * classes.
     *
     * @param jpaMetaModelClasses the set of classes on the classpath annotated
     * with {@link StaticMetamodel}
     * @return a descriptor for each meta model.
     */
    public SortedSet<TypeDescriptor> generate(Set<Class<?>> jpaMetaModelClasses) {
        final SortedSet<TypeDescriptor> retval = new TreeSet<>();
        addManagedTypeDescriptors(retval, jpaMetaModelClasses);
        addSuperDescriptors(jpaMetaModelClasses);
        addBasicTypeDescriptors(retval, jpaMetaModelClasses);
        addAttributeTypeDescriptors(retval, jpaMetaModelClasses);
        return retval;
    }

    /**
     * Add all of the managed type descriptors to the {@code typeDescriptors}.
     *
     * @param typeDescriptors The set to put the created managed type
     * descriptors into.
     * @param jpaMetaModelClasses The set of Java classes to create the
     * descriptors for.
     * @throws IllegalStateException The persistence type cannot be determined.
     */
    private void addManagedTypeDescriptors(final SortedSet<TypeDescriptor> typeDescriptors, Set<Class<?>> jpaMetaModelClasses) throws IllegalStateException {
        final TreeSet<Class> sortedClasses = new TreeSet<>(CLASS_COMPARATOR);
        sortedClasses.addAll(jpaMetaModelClasses);
        for (Class<?> managedClass : sortedClasses) {
            final Class javaType = managedClass.getAnnotation(StaticMetamodel.class).value();
            final TypeDescriptor td;
            if (javaType.getAnnotation(Embeddable.class) != null) {
                //embeddable
                td = new EmbeddableTypeDescriptor(javaType.getSimpleName(), javaType, ordinal.getAndIncrement());
            } else if (javaType.getAnnotation(MappedSuperclass.class) != null) {
                //mapped superclass
                td = new MappedSuperClassDescriptor(javaType.getSimpleName(), javaType, ordinal.getAndIncrement());
            } else if (javaType.getAnnotation(Entity.class) != null) {
                //entity
                td = new EntityTypeDescriptor(javaType.getSimpleName(), javaType, ordinal.getAndIncrement());
            } else {
                throw new IllegalStateException("unable to determine PersistenceType for " + managedClass);
            }
            typeDescriptors.add(td);
        }
    }

    /**
     * Add the super descriptor to each type descriptor.
     * @param jpaMetaModels a set of all the found JPA meta model classes.
     */
    private void addSuperDescriptors(Set<Class<?>> jpaMetaModels) {
        for (Class<?> jpaMetaModel : jpaMetaModels) {
            try {
                //Find the corresponding type descriptor.
                Class originalClass = Class.forName(removeTrailingUnderscore(jpaMetaModel.getName()));
                TypeDescriptor td = TypeDescriptor.getInstance(originalClass);
                if (td != null) {
                    Class superclass = jpaMetaModel.getSuperclass();
                    if (superclass != null && !superclass.getSimpleName().equals("Object")) {
                        String baseName = removeTrailingUnderscore(superclass.getName());
                        try {
                            Class parentClass = Class.forName(baseName);
                            TypeDescriptor foundTd = TypeDescriptor.getInstance(parentClass);
                            if (foundTd != null) {
                                td.setSuperDescriptor(foundTd);
                            } else {
                                //Could not find the type descriptor of the super class.
                                throw new IllegalStateException("Could not find the type descriptor for the super class of: " + parentClass.getSimpleName());
                            }
                        } catch (ClassNotFoundException ex) {
                            //Descriptor has no superclass, so ignore exception.
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                //Could not find the type descriptor for the jpa class.
                throw new IllegalStateException("Could not find the type descriptor for the jpa meta model class: " + jpaMetaModel.getSimpleName());
            }
        }
    }

    /**
     * Add all of the basic type descriptors to the
     * {@code managedTypeDescriptors}.
     *
     * @param managedTypeDescriptors The set of managed type descriptors
     * determined earlier.
     * @param jpaMetaModelClasses The set of meta model classes to create the
     * basic type descriptors from.
     * @throws IllegalStateException If the persistence type could not be
     * determined.
     */
    private void addBasicTypeDescriptors(final SortedSet<TypeDescriptor> managedTypeDescriptors, Set<Class<?>> jpaMetaModelClasses) throws IllegalStateException {
        SortedSet<BasicTypeDescriptor> basicTypeDescriptors = new TreeSet<>();
        for (Class<?> metaModelClass : jpaMetaModelClasses) {
            for (Field metaModelField : metaModelClass.getDeclaredFields()) {
                final Class<?> metaModelFieldType = metaModelField.getType();
                final Class[] attributeJavaTypes = getGenericTypes(metaModelField, getGenericTypeIndices(metaModelFieldType));
                for (Class attributeJavaType : attributeJavaTypes) {
                    final TypeDescriptor td = TypeDescriptor.getInstance(attributeJavaType);
                    if (td == null) {
                        basicTypeDescriptors.add(
                                new BasicTypeDescriptor(attributeJavaType.getSimpleName(), attributeJavaType, ordinal.getAndIncrement())
                        );
                    }
                }
                if (PluralAttribute.class.isAssignableFrom(metaModelFieldType)) {
                    final Class javaFieldType = PLURAL_ATTRIBUTE_TO_COLLECTION_CLASS.get((Class<? extends PluralAttribute>) metaModelFieldType);
                    final TypeDescriptor td = TypeDescriptor.getInstance(javaFieldType);
                    if (td == null) {
                        basicTypeDescriptors.add(
                                new BasicTypeDescriptor(javaFieldType.getSimpleName(), javaFieldType, ordinal.getAndIncrement())
                        );
                    }
                }
            }
        }
        managedTypeDescriptors.addAll(basicTypeDescriptors);
    }

    /**
     * Add all the attributes to each found {@link TypeDescriptor}.
     *
     * @param managedTypeDescriptors The set of {@link ManagedTypeDescriptor}s
     * that have been found.
     * @param jpaMetaModelClasses The JPA meta model classes that are found in
     * the domain project.
     */
    private void addAttributeTypeDescriptors(final SortedSet<TypeDescriptor> managedTypeDescriptors, Set<Class<?>> jpaMetaModelClasses) {
        for (Class<?> metaModelClass : jpaMetaModelClasses) {
            final SortedSet<AttributeDescriptor> classAttributes = new TreeSet<>();
            ManagedTypeDescriptor modelDescriptor = null;

            //Look for the corresponding type descriptor to add the attributes to.
            String modelName = metaModelClass.getSimpleName().substring(0, metaModelClass.getSimpleName().length() - 1); //Remove the trailing underscore.
            for (TypeDescriptor td : managedTypeDescriptors) {
                if (td.getName().equals(modelName)) {
                    modelDescriptor = (ManagedTypeDescriptor) td;
                    break;
                }
            }

            for (Field metaModelField : metaModelClass.getDeclaredFields()) {
                final Class<?> metaModelFieldType = metaModelField.getType();
                final Class[] attributeJavaTypes = getGenericTypes(metaModelField, getGenericTypeIndices(metaModelFieldType));
                AttributeDescriptor ad = null;

                if (metaModelFieldType == SingularAttribute.class) {
                    ad = new SingularAttributeDescriptor(metaModelField.getName(), attributeJavaTypes[0], ordinal.getAndIncrement());
                } else if (PluralAttribute.class.isAssignableFrom(metaModelFieldType)) {
                    final Class javaFieldType = PLURAL_ATTRIBUTE_TO_COLLECTION_CLASS.get((Class<? extends PluralAttribute>) metaModelFieldType);
                    TypeDescriptor td = TypeDescriptor.getInstance(attributeJavaTypes[0]);
                    //Possibly make type descriptor if it does not exist?

                    if (javaFieldType == Collection.class) {
                        ad = new CollectionAttributeDescriptor(td, metaModelField.getName(), attributeJavaTypes[0], ordinal.getAndIncrement());
                    } else if (javaFieldType == List.class) {
                        ad = new ListAttributeDescriptor(td, metaModelField.getName(), attributeJavaTypes[0], ordinal.getAndIncrement());
                    } else if (javaFieldType == Map.class) {
                        ad = new MapAttributeDescriptor(td, metaModelField.getName(), attributeJavaTypes[1], attributeJavaTypes[0], ordinal.getAndIncrement());
                    } else {
                        //Set
                        ad = new SetAttributeDescriptor(td, metaModelField.getName(), attributeJavaTypes[0], ordinal.getAndIncrement());
                    }
                }
                if (ad != null) {
                    ad.setDeclaringTypeDescriptor(modelDescriptor);
                    //TODO set super descriptor.
                    classAttributes.add(ad);
                }
            }
            if (modelDescriptor != null) {
                modelDescriptor.setAttributes(classAttributes);
            } else {
                log.warn("Could not find the type descriptor for: " + metaModelClass.getSimpleName());
            }
        }
    }

    /**
     * Calculate which indices to get when getting the generic types.
     *
     * @param metaModelFieldType The type of the current field.
     * @return an array of ints with the indices to retrieve with the
     * getGenericTypes() method.
     * @see TypeGenerator#getGenericTypes(java.lang.reflect.Field, int...)
     */
    private int[] getGenericTypeIndices(final Class<?> metaModelFieldType) {
        int[] indices;
        if (metaModelFieldType == MapAttribute.class) {
            indices = new int[]{-1, -2};
        } else {
            indices = new int[]{-1};
        }
        return indices;
    }

    /**
     * Removes the trailing underscore from jpa meta model class names. Or
     * otherwise can be used to remove the last character of a string.
     *
     * @param metaModelClassName the class name of the jpa meta model to remove
     * the trialing underscore from.
     * @return the {@code metaModelClassName} string with the last character removed.
     * or the original {@code metaModelClassName} if the length was insuffecient.
     */
    private String removeTrailingUnderscore(String metaModelClassName) {
        if (metaModelClassName.length() > 1) {
            return metaModelClassName.substring(0, metaModelClassName.length() - 1);
        }
        return metaModelClassName;
    }

    /**
     * Get the generic types for a certain field.
     *
     * @param field The {@link Field} to get the generic types of.
     * @param indices The indices for the generic type parameters to get.
     * @return An array of {@link Class}es with the found generic types.
     */
    private Class[] getGenericTypes(Field field, int... indices) {
        final Class[] retval = new Class[indices.length];
        final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        final Type[] actualTypeArguments = genericType.getActualTypeArguments();
        int i = 0;
        for (int index : indices) {
            final int realIndex = index < 0 ? actualTypeArguments.length + index : index;
            retval[i++] = (Class) actualTypeArguments[realIndex];
        }
        return retval;
    }
}