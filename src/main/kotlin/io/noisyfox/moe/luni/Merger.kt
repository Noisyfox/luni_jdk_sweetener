package io.noisyfox.moe.luni

import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.TypeDeclaration
import java.io.File

class Merger(
    val desugarLibClassPath: String,
    val luniOutputPath: String,
    luniClassPath: String,
) {
    private val luniClassPaths = luniClassPath
        .split(File.pathSeparator)
        .distinct()
        .map(::File)
        .filter(File::exists)

    init {
        luniClassPaths.forEach {
            require(it.isDirectory)
        }
    }

    fun merge() {
        desugarLibClassPath
            .split(File.pathSeparator)
            .distinct()
            .forEach { desugarCP ->
                File(desugarCP)
                    // Scan the desugar lib
                    .walk()
                    // Only process java file
                    .filter {
                        it.isFile
                                && it.extension.toLowerCase() == "java"
                                // Ignore package-info.java
                                && it.name.toLowerCase() != "package-info.java"
                    }
                    .forEach(::process)
            }
    }

    private fun process(sugarFile: File) {
//        println(sugarFile)

        // Create the AST parser
        val parser = ASTParser.newParser(AST.JLS8)
        parser.setSource(
            // Read the content of the file
            sugarFile.readText().toCharArray()
        )
        // Read the AST
        val astRoot = parser.createAST(null) as CompilationUnit

        // Check the file type
        if (sugarFile.name.startsWith(CLASS_PREFIX_DESUGAR)) {
            processDesugarFile(astRoot)
        } else {
        }
    }

    private fun processDesugarFile(astRoot: CompilationUnit) {
        @Suppress("UNCHECKED_CAST")
        val ts = astRoot.getStructuralProperty(CompilationUnit.TYPES_PROPERTY) as List<TypeDeclaration>
        val clazz = ts.single()

        // Validate input
        val name = clazz.name.identifier
        require(name.startsWith(CLASS_PREFIX_DESUGAR))
        require(!clazz.isInterface)
        require(clazz.superclassType == null)
        require(clazz.superInterfaceTypes().isEmpty())

        // Determine the dest class
        val destName = name.substring(CLASS_PREFIX_DESUGAR.length)
        val destPackage = astRoot.`package`.name.fullyQualifiedName
        val destFQ = if (destPackage.isEmpty()) {
            destName
        } else {
            "$destPackage.$destName"
        }.let {
            // TODO: configurable
            if (it == "java.util.concurrent.Unsafe") {
                "sun.misc.Unsafe"
            } else {
                it
            }
        }
//        println(destFQ)

        // Find the dest file to update
        val destFile = requireLuniFile(destFQ)
        println(destFile.absolutePath)


    }

    private fun requireLuniFile(fullQualifiedName: String): File {
        val destPath = fullQualifiedName.replace(".", File.separator) + ".java"

        val f = luniClassPaths
            .map { File(it, destPath) }
            .single { it.exists() }

        require(f.isFile)

        return f
    }

    companion object {
        private const val CLASS_PREFIX_DESUGAR = "Desugar"
    }
}