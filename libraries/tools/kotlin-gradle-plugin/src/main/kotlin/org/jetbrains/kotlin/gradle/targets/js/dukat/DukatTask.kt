/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.dukat

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJsCompilation
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import java.io.File
import javax.inject.Inject

open class DukatTask
@Inject
constructor(
    compilation: KotlinJsCompilation
) : AbstractDukatTask(compilation) {
    private val nodeJs get() = NodeJsRootPlugin.apply(project.rootProject)

    @get:Internal
    val dts by lazy {
        val resolvedCompilation = nodeJs.npmResolutionManager.requireInstalled()[project][compilation]
        val dtsResolver = DtsResolver(resolvedCompilation.npmProject)
        dtsResolver.getAllDts(resolvedCompilation.externalNpmDependencies)
    }

    @get:Internal
    override val dTsFiles: List<File>
        get() = dts.map { it.file }

    @get:Input
    val inputs
        get() = dts.map { it.inputKey }

    @get:OutputDirectory
    override val destDir: File
        get() = compilation.npmProject.externalsDir

    private val executor by lazy {
        DukatExecutor(nodeJs, dts, compilation.npmProject, true, compareInputs = false)
    }

    override fun run() {
        executor.execute()
    }
}