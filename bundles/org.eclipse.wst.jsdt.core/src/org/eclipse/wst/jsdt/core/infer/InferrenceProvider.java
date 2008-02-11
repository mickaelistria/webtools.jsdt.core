package org.eclipse.wst.jsdt.core.infer;

import org.eclipse.wst.jsdt.core.ast.IScriptFileDeclaration;

/**
 *  Implemented by extenders of org.eclipse.wst.jsdt.core.infer.inferrenceSupport extension point
 */
public interface InferrenceProvider {
	
	public static final int ONLY_THIS = 1;
	public static final int NOT_THIS = 2;
	public static final int MAYBE_THIS = 3;
	
	/**
	 * Get the inference engine for this inference provider
	 * @return Inference engine  
	 */
	public InferEngine getInferEngine();
	
	
	/**
	 * Determine if this inference provider applies to a script
	 * @param scriptFile The script that the inferencing will be done for
	 * @return  InferrenceProvider.ONLY_THIS, InferrenceProvider.NOT_THIS, or InferrenceProvider.MAYBE_THIS, depending on how much
	 * this inference provider applies to the specified script.
	 */
	public int applysTo(IScriptFileDeclaration scriptFile);
	/**
	 * Get the inference provider ID
	 * @return the id of this inference provider
	 */
	public String getID();
	
}