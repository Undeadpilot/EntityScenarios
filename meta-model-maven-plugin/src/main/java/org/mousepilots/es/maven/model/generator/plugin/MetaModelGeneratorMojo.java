package org.mousepilots.es.maven.model.generator.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.metamodel.StaticMetamodel;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.app.VelocityEngine;
import org.mousepilots.es.maven.model.generator.controller.TypeGenerator;
import org.mousepilots.es.maven.model.generator.model.AssociationDescriptor;
import org.mousepilots.es.maven.model.generator.model.Descriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.AttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.CollectionAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.ListAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.MapAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.SetAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.attribute.SingularAttributeDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.ManagedTypeDescriptor;
import org.mousepilots.es.maven.model.generator.model.type.TypeDescriptor;
import org.mousepilots.es.core.model.AssociationTypeES;
import org.reflections.Reflections;

@Mojo(name = "generate",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        executionStrategy = "once-per-session"
)
public class MetaModelGeneratorMojo extends AbstractMojo {

    /**
     * used to prevent multiple executions within the same maven session
     */
    private static boolean executed = false;

    /**
     * The fully qualified name of the package in which sources are generated by
     * the plugin. E.g. {@code org.my.package}. The plugin must have this and
     * all of its subpackages to itself
     */
    @Parameter(required = true)
    private String packageName;

    /**
     * The maven project that is executing this plugin during its compile phase.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The project folder in which generated sources are put. The plugin should
     * have this folder and all subfolders to itself.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/mmx")
    private File generatedSourceDir;

    /**
     * Contains an in-memory representation of the reflections xml report of the
     * domain project
     */
    private Reflections reflections;

    /**
     * Contains the name and version of this plugin.
     * Which will be inserted in the generated classes.
     */
    private static final String ES_NAME_AND_VERSION = getESNameAndVersion();

    /**
     * Contains the current date.
     * Which will be inserted in the generated classes.
     */
    private static final Date currentDate = new Date();

    /**
     * The velocity engine for generating source files
     */
    private final VelocityEngine velocityEngine;

    public MetaModelGeneratorMojo() {
        velocityEngine = new VelocityEngine();
        Properties props = new Properties();
        props.put("resource.loader", "class");
        props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.put("directive.set.null.allowed", Boolean.TRUE.toString());
        velocityEngine.init(props);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (executed) {
            return;
        } else {
            executed = true;
        }
        getLog().info("Starting meta model generation.");
        initGeneratedSourcesDir();
        getLog().info("Initialized the generated sources folder: " + this.generatedSourceDir.getAbsolutePath());
        reflections = new Reflections((String) null);
        final Set<Class<?>> jpaMetaModelClasses = reflections.getTypesAnnotatedWith(StaticMetamodel.class);
        if (jpaMetaModelClasses.isEmpty()) {
            //No meta models found.
            getLog().info("No meta model classes where found, stopping execution.");
            return;
        }
        getLog().info("Found " + jpaMetaModelClasses.size() + " meta model classes.");
        SortedSet<TypeDescriptor> generatedTypes = null;
        try {
            final TypeGenerator generator = new TypeGenerator(getLog());
            generatedTypes = generator.generate(jpaMetaModelClasses);
        } catch (IllegalStateException ex) {
            getLog().error("Generator failed to generate types.", ex);
            throw new MojoFailureException("Generator failed to generate types", ex);
        }
//        TODO generate actual files.
        if (generatedTypes == null) {
            throw new MojoFailureException("Generator failed to generate types");
        }
        printGeneratedTypes(generatedTypes);
        getLog().info("Successfully completed meta model generation");
    }

    /**
     * Cleans a certain directory by removing all files and subfolders
     * recursivly.
     *
     * @param directory the directory to clean.
     */
    private void clean(File directory) {
        if (directory != null && directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    clean(file);
                }
                file.delete();
            }
        }
    }

    /**
     * Initializes the generated sources directory for the creation of the
     * generated meta model classes.
     *
     * @throws MojoFailureException if the generated sources directory could not
     * be created if it did not exist.
     */
    private void initGeneratedSourcesDir() throws MojoFailureException {
        if (generatedSourceDir.exists()) {
            clean(generatedSourceDir);
        } else {
            if (!generatedSourceDir.mkdirs()) {
                final String message = "failed to create generated sources directory " + generatedSourceDir.getAbsolutePath();
                throw new MojoFailureException(message);
            }
        }
        project.addCompileSourceRoot(generatedSourceDir.getAbsolutePath());
    }

    /**
     * Print information about the type descriptors that were generated. For
     * Debugging purposes untill actual classes are generated.
     *
     * @param generatedTypes The type descriptors that were generated.
     */
    private void printGeneratedTypes(SortedSet<TypeDescriptor> generatedTypes) {
        for (TypeDescriptor td : generatedTypes) {
            if (ManagedTypeDescriptor.class.isAssignableFrom(td.getClass())) {
                ManagedTypeDescriptor mtd = (ManagedTypeDescriptor) td;
                StringBuilder sb = new StringBuilder("Modeled class: ").append(mtd.getName());
                sb.append("\nPersistence type: ").append(mtd.getPersistenceType());
                sb.append("\nSuper descriptor: ").append(mtd.getSuperDescriptor() != null ? mtd.getSuperDescriptor().getName() : "null");
                sb.append("\nFound attributes ------------------\n");
                for (AttributeDescriptor ad : mtd.getAttributes()) {
                    if (MapAttributeDescriptor.class.isAssignableFrom(ad.getClass())) {
                        MapAttributeDescriptor mad = (MapAttributeDescriptor) ad;
                        sb.append("Attribute: ").append(mad.getName()).append(" Type: ").append(mad.getCollectionType()).append(", Key type: ").append(mad.getKeyJavaType()).append(", Element type: ").append(mad.getElementType().getName());
                    } else if (CollectionAttributeDescriptor.class.isAssignableFrom(ad.getClass())) {
                        CollectionAttributeDescriptor cad = (CollectionAttributeDescriptor) ad;
                        sb.append("Attribute: ").append(cad.getName()).append(" Type: ").append(cad.getCollectionType()).append(", Element type: ").append(cad.getElementType().getName());
                    } else if (ListAttributeDescriptor.class.isAssignableFrom(ad.getClass())) {
                        ListAttributeDescriptor cad = (ListAttributeDescriptor) ad;
                        sb.append("Attribute: ").append(cad.getName()).append(" Type: ").append(cad.getCollectionType()).append(", Element type: ").append(cad.getElementType().getName());
                    } else if (SetAttributeDescriptor.class.isAssignableFrom(ad.getClass())) {
                        SetAttributeDescriptor cad = (SetAttributeDescriptor) ad;
                        sb.append("Attribute: ").append(cad.getName()).append(" Type: ").append(cad.getCollectionType()).append(", Element type: ").append(cad.getElementType().getName());
                    } else {
                        //Singular
                        SingularAttributeDescriptor sad = (SingularAttributeDescriptor) ad;
                        sb.append("Attribute: ").append(sad.getName()).append(", Java type: ").append(sad.getJavaTypeSimpleName());
                    }
                    sb.append("\n\r");

                    //Try to get associations.
                    AssociationDescriptor keyAssoc = ad.getAssociation(AssociationTypeES.KEY);
                    AssociationDescriptor valueAssoc = ad.getAssociation(AssociationTypeES.VALUE);
                    if (keyAssoc != null) {
                        sb.append("Key association: ").append(keyAssoc.toString()).append("\n\r");
                    }
                    if (valueAssoc != null) {
                        sb.append("Value association: ").append(valueAssoc.toString()).append("\n\r");
                    }
                }
                getLog().info(sb.toString());
            }
        }
    }

    /**
     * Load the name of the plugin and its version from the properties file.
     * So it can be used to annotate the generated meta model classes.
     * @return A {@link String} containing the name and version of the plugin.
     */
    private static String getESNameAndVersion() {
        final Properties properties = new Properties();
        final StringBuilder sb = new StringBuilder();
        try {
            InputStream resourceAsStream = Descriptor.class.getResourceAsStream(
                    "/project.properties");
            if (resourceAsStream != null) {
                properties.load(resourceAsStream);
                sb.append(properties.getProperty("artifactId", "Unknown"));
                sb.append("_");
                sb.append(properties.getProperty("version", "unknown version"));
            } else {
                sb.append("Unknown_unknown version");
            }
        } catch (IOException ex) {
            Logger.getLogger(Descriptor.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
        return sb.toString();
    }
}