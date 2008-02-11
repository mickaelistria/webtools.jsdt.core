/**
 *
 */
package org.eclipse.wst.jsdt.core;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.wst.jsdt.core.compiler.libraries.LibraryLocation;

/**
 * @author childsb
 *
 */
public interface IJsGlobalScopeContainerInitialzer {
	/**
	 * Binds a classpath container to a <code>IJsGlobalScopeContainer</code> for a given project,
	 * or silently fails if unable to do so.
	 * <p>
	 * A container is identified by a container path, which must be formed of two segments.
	 * The first segment is used as a unique identifier (which this initializer did register onto), and
	 * the second segment can be used as an additional hint when performing the resolution.
	 * <p>
	 * The initializer is invoked if a container path needs to be resolved for a given project, and no
	 * value for it was recorded so far. The implementation of the initializer would typically set the
	 * corresponding container using <code>JavaCore#setJsGlobalScopeContainer</code>.
	 * <p>
	 * A container initialization can be indirectly performed while attempting to resolve a project
	 * classpath using <code>IJavaProject#getResolvedClasspath(</code>; or directly when using
	 * <code>JavaCore#getJsGlobalScopeContainer</code>. During the initialization process, any attempt
	 * to further obtain the same container will simply return <code>null</code> so as to avoid an
	 * infinite regression of initializations.
	 * <p>
	 * A container initialization may also occur indirectly when setting a project classpath, as the operation
	 * needs to resolve the classpath for validation purpose. While the operation is in progress, a referenced
	 * container initializer may be invoked. If the initializer further tries to access the referring project classpath,
	 * it will not see the new assigned classpath until the operation has completed. Note that once the Java
	 * change notification occurs (at the end of the operation), the model has been updated, and the project
	 * classpath can be queried normally.
	 * <p>
	 * This method is called by the Java model to give the party that defined
	 * this particular kind of classpath container the chance to install
	 * classpath container objects that will be used to convert classpath
	 * container entries into simpler classpath entries. The method is typically
	 * called exactly once for a given Java project and classpath container
	 * entry. This method must not be called by other clients.
	 * <p>
	 * There are a wide variety of conditions under which this method may be
	 * invoked. To ensure that the implementation does not interfere with
	 * correct functioning of the Java model, the implementation should use
	 * only the following Java model APIs:
	 * <ul>
	 * <li>{@link JavaCore#setJsGlobalScopeContainer(IPath, IJavaProject[], IJsGlobalScopeContainer[], org.eclipse.core.runtime.IProgressMonitor)}</li>
	 * <li>{@link JavaCore#getJsGlobalScopeContainer(IPath, IJavaProject)}</li>
	 * <li>{@link JavaCore#create(org.eclipse.core.resources.IWorkspaceRoot)}</li>
	 * <li>{@link JavaCore#create(org.eclipse.core.resources.IProject)}</li>
	 * <li>{@link IJavaModel#getJavaProjects()}</li>
	 * <li>Java element operations marked as "handle-only"</li>
	 * </ul>
	 * The effects of using other Java model APIs are unspecified.
	 * </p>
	 *
	 * @param containerPath a two-segment path (ID/hint) identifying the container that needs
	 * 	to be resolved
	 * @param project the Java project in which context the container is to be resolved.
	 *    This allows generic containers to be bound with project specific values.
	 * @throws CoreException if an exception occurs during the initialization
	 *
	 * @see JavaCore#getJsGlobalScopeContainer(IPath, IJavaProject)
	 * @see JavaCore#setJsGlobalScopeContainer(IPath, IJavaProject[], IJsGlobalScopeContainer[], org.eclipse.core.runtime.IProgressMonitor)
	 * @see IJsGlobalScopeContainer
	 */
	public abstract void initialize(IPath containerPath, IJavaProject project) throws CoreException;

	/**
	 * Returns <code>true</code> if this container initializer can be requested to perform updates
	 * on its own container values. If so, then an update request will be performed using
	 * <code>JsGlobalScopeContainerInitializer#requestJsGlobalScopeContainerUpdate</code>/
	 * <p>
	 * @param containerPath the path of the container which requires to be updated
	 * @param project the project for which the container is to be updated
	 * @return returns <code>true</code> if the container can be updated
	 * @since 2.1
	 */
	public abstract boolean canUpdateJsGlobalScopeContainer(IPath containerPath, IJavaProject project);

	/**
	 * Request a registered container definition to be updated according to a container suggestion. The container suggestion
	 * only acts as a place-holder to pass along the information to update the matching container definition(s) held by the
	 * container initializer. In particular, it is not expected to store the container suggestion as is, but rather adjust
	 * the actual container definition based on suggested changes.
	 * <p>
	 * IMPORTANT: In reaction to receiving an update request, a container initializer will update the corresponding
	 * container definition (after reconciling changes) at its earliest convenience, using
	 * <code>JavaCore#setJsGlobalScopeContainer(IPath, IJavaProject[], IJsGlobalScopeContainer[], IProgressMonitor)</code>.
	 * Until it does so, the update will not be reflected in the Java Model.
	 * <p>
	 * In order to anticipate whether the container initializer allows to update its containers, the predicate
	 * <code>JavaCore#canUpdateJsGlobalScopeContainer</code> should be used.
	 * <p>
	 * @param containerPath the path of the container which requires to be updated
	 * @param project the project for which the container is to be updated
	 * @param containerSuggestion a suggestion to update the corresponding container definition
	 * @throws CoreException when <code>JavaCore#setJsGlobalScopeContainer</code> would throw any.
	 * @see JavaCore#setJsGlobalScopeContainer(IPath, IJavaProject[], IJsGlobalScopeContainer[], org.eclipse.core.runtime.IProgressMonitor)
	 * @see JsGlobalScopeContainerInitializer#canUpdateJsGlobalScopeContainer(IPath, IJavaProject)
	 * @since 2.1
	 */
	public abstract void requestJsGlobalScopeContainerUpdate(IPath containerPath, IJavaProject project, IJsGlobalScopeContainer containerSuggestion)
			throws CoreException;

	/**
	 * Returns a readable description for a container path. A readable description for a container path can be
	 * used for improving the display of references to container, without actually needing to resolve them.
	 * A good implementation should answer a description consistent with the description of the associated
	 * target container (see <code>IJsGlobalScopeContainer.getDescription()</code>).
	 *
	 * @param containerPath the path of the container which requires a readable description
	 * @param project the project from which the container is referenced
	 * @return a string description of the container
	 * @since 2.1
	 */
	public abstract String getDescription(IPath containerPath, IJavaProject project);

	/**
	 * Returns a classpath container that is used after this initializer failed to bind a classpath container
	 * to a <code>IJsGlobalScopeContainer</code> for the given project. A non-<code>null</code>
	 * failure container indicates that there will be no more request to initialize the given container
	 * for the given project.
	 * <p>
	 * By default a non-<code>null</code> failure container with no classpath entries is returned.
	 * Clients wishing to get a chance to run the initializer again should override this method
	 * and return <code>null</code>.
	 * </p>
	 *
	 * @param containerPath the path of the container which failed to initialize
	 * @param project the project from which the container is referenced
	 * @return the default failure container, or <code>null</code> if wishing to run the initializer again
	 * @since 3.3
	 */
	public abstract IJsGlobalScopeContainer getFailureContainer(final IPath containerPath, IJavaProject project);

	/**
	 * Returns an object which identifies a container for comparison purpose. This allows
	 * to eliminate redundant containers when accumulating classpath entries (e.g.
	 * runtime classpath computation). When requesting a container comparison ID, one
	 * should ensure using its corresponding container initializer. Indeed, a random container
	 * initializer cannot be held responsible for determining comparison IDs for arbitrary
	 * containers.
	 * <p>
	 * @param containerPath the path of the container which is being checked
	 * @param project the project for which the container is to being checked
	 * @return returns an Object identifying the container for comparison
	 * @since 3.0
	 */
	public abstract Object getComparisonID(IPath containerPath, IJavaProject project);

	public abstract URI getHostPath(IPath path, IJavaProject project);

	LibraryLocation getLibraryLocation();
	/*
	 * Returns if this library allows attachment of external JsDoc
	 */
	boolean allowAttachJsDoc();
	/*
	 * returns a String of all SuperTypes provided by this library.
	 */
	String[] containerSuperTypes();
	
	/**
	 * Get the id of the inference provider for this library
	 * @return  inference provider id
	 */
	String getInferenceID();
}