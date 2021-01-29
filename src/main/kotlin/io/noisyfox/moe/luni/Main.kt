package io.noisyfox.moe.luni

fun main() {
    // TODO: parse arg
    val desugarLibClassPath = "F:\\MyProject\\moe\\desugar_jdk_libs\\src\\share\\classes"
    val luniOutputPath = "F:\\MyProject\\moe\\moe\\aosp\\libcore\\luni\\src\\main\\java"
    val luniClassPath = "$luniOutputPath;F:\\MyProject\\moe\\moe\\aosp\\libcore\\libart\\src\\main\\java"

    val merger = Merger(
        desugarLibClassPath = desugarLibClassPath,
        luniOutputPath = luniOutputPath,
        luniClassPath = luniClassPath,
    )
    merger.merge()
}
