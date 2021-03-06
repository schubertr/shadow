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

package shadow.doctool;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import shadow.ConfigurationException;
import shadow.parser.javacc.Node;
import shadow.parser.javacc.ParseException;
import shadow.parser.javacc.ShadowException;
import shadow.typecheck.BaseChecker;
import shadow.typecheck.Package;
import shadow.typecheck.TypeCheckException;
import shadow.typecheck.TypeCollector;
import shadow.typecheck.TypeUpdater;
import shadow.typecheck.type.Type;

/** 
 * A basic type-checker used to gather enough data for documentation.
 * @author Brian Stottler
 */
public class DocumentationTypeChecker {	
	/**
	 * Checks the types in the given files and returns a set containing those types,
	 * ready to be documented.
	 * @param files				list of files
	 * @return					set of types in those files
	 * @throws ShadowException
	 * @throws TypeCheckException
	 * @throws ParseException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static Set<Type> typeCheck( List<File> files )
			throws ShadowException, TypeCheckException, ParseException, IOException, ConfigurationException {			
		Package packageTree = new Package();		
		
		/* Collector looks over all files and creates types for everything needed. */
		TypeCollector collector = new TypeCollector( packageTree, true );
		/* Its return value maps all the types to their AST nodes. */		
		Map<Type, Node> typeTable = collector.collectTypes( files );
		
		/* Updates types, adding:
		 *  Fields and methods
		 *  Type parameters (including necessary instantiations)
		 *  All types with type parameters (except for declarations) are UninitializedTypes
		 *  Extends and implements lists
		 */	
		TypeUpdater updater = new TypeUpdater( packageTree );
		typeTable = updater.update( typeTable );
		
		/* Filter out only those types associated with the files being documented. */		
		Set<Type> types = new TreeSet<Type>();
		// Maps file names to nodes.
		Map<String, Node> fileTable = collector.getFileTable();
		for( File file : files ) {
			Node node = fileTable.get( BaseChecker.stripExtension( file.getCanonicalPath() ) );
			if( node != null )
				types.add( node.getType() );
		}
		
		return types;
	}
}
