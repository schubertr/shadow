/*
 * Copyright 2015 Team Shadow
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 	
 * 	    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package shadow.typecheck;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import shadow.ConfigurationException;
import shadow.Loggers;
import shadow.parser.javacc.Node;
import shadow.parser.javacc.ParseException;
import shadow.parser.javacc.ShadowException;
import shadow.typecheck.type.ArrayType;
import shadow.typecheck.type.Type;

/**
 * Utility class to hold static type-checking methods. 
 * @author Barry Wittman
 */
public class TypeChecker {
	/**
	 * Typechecks a main file and all files that it depends on.
	 * @param file 				the main file to compile
	 * @param useSourceFiles 	whether the source files should be recompiled
	 * 
	 * @return nodes list of AST nodes for the classes to be compile
	 * @throws ShadowException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ConfigurationException 
	 */
	public static List<Node> typeCheck(File file, boolean useSourceFiles)
			throws ShadowException, ParseException, TypeCheckException, IOException, ConfigurationException {	
		
		Package packageTree = new Package(); // Root of all packages, storing all types		
		
		/* Collector looks over all files and creates types for everything needed. */
		TypeCollector collector = new TypeCollector( packageTree, useSourceFiles );
		
		/* Its return value maps all the types to the nodes that need compiling. */		
		Map<Type, Node> nodeTable = collector.collectTypes( file );
		Type mainType = collector.getMainType();
		
		/* Updates types, adding:
		 *  Fields and methods
		 *  Type parameters (including necessary instantiations)
		 *  All types with type parameters (except for declarations) are UninitializedTypes
		 *  Extends and implements lists
		 */				
		TypeUpdater updater = new TypeUpdater(packageTree);
		nodeTable = updater.update( nodeTable );
		
		/* Select only nodes corresponding to outer types. */				
		List<Node> allNodes = new ArrayList<Node>();
		for( Node node : nodeTable.values())
			if( !node.getType().hasOuter() )
				allNodes.add(node);
		
		/* Do type-checking of statements, i.e., actual code. */
		StatementChecker checker = new StatementChecker( packageTree );
		for( Node node: allNodes ) {	
			File nodeFile = node.getFile();
			if( !nodeFile.getPath().endsWith(".meta")) {
				/* Check all statements for type safety and other features */
				checker.check(node);				
				/* As an optimization, print .meta file for the .shadow file being checked. */
				printMetaFile( node, BaseChecker.stripExtension( nodeFile.getCanonicalPath() ) );
			}
		}
		
		/* After type-checking, we can determine which types are referenced
		 * by the main type (even indirectly). */
		TreeSet<Type> referencedTypes = new TreeSet<Type>();
		referencedTypes.add(mainType); // almost everything gets figured out from there
		addStandardTypes(referencedTypes);
		
		/* Determine which types are needed for the current compilation. */
		Set<Type> neededTypes = new HashSet<Type>();
		while( !referencedTypes.isEmpty() ) {			
			Type next = referencedTypes.first();
			Type simplified = next.getTypeWithoutTypeArguments();			
			if( !(simplified instanceof ArrayType) && !simplified.hasOuter() && neededTypes.add( simplified ) )				
				referencedTypes.addAll( simplified.getUsedTypes() );
			
			referencedTypes.remove(next);
		}
		
		/* Return only those nodes corresponding to needed types. */
		List<Node> neededNodes = new ArrayList<Node>();		
		for( Node node : allNodes )
			if( neededTypes.contains( node.getType() ) )
				neededNodes.add(node);
		
		return neededNodes;
	}
	
	/*
	 * Prints a .meta file version of a given node, similar to a header file in C/C++.
	 * These .meta files are used for type-checking as a speed optimization, to avoid 
	 * type-checking the full code.
	 */
	private static void printMetaFile( Node node, String file ) {
		try {
			File shadowVersion = new File( file + ".shadow");
			File metaVersion = new File( file + ".meta");
			/* Add meta file if an updated one doesn't already exist. */
			if( !metaVersion.exists() || (shadowVersion.exists() &&
					shadowVersion.lastModified() >= metaVersion.lastModified()) ) {	
				PrintWriter out = new PrintWriter(metaVersion);
				node.getType().printMetaFile(out, "");
				out.close();						
			}
		}
		catch( IOException e ) {
			Loggers.SHADOW.error("Failed to create meta file for " + node.getType() );					
		}		
	}	
	
	/*
	 * Adds a list of standard types that are needed regardless of the compilation.
	 * These types may not be directly referenced,
	 * but they are referenced indirectly by the compiler.
	 */
	private static void addStandardTypes( Set<Type> types ) {
		Package standard = Type.OBJECT.getPackage(); // shadow:standard package
		types.addAll( standard.getTypes() );		
		
		/* A few io classes are absolutely necessary for a console program. */
		Package io = standard.getParent().getChild("io");
		types.add( io.getType( "File" ) );
		types.add( io.getType( "IOException" ) );
		types.add( io.getType( "Path" ) );
	}
}
