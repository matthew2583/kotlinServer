package ru.yarsu.cmd

import com.beust.jcommander.ParametersDelegate
import ru.yarsu.args.PathArgs

open class BaseCmd {
    @ParametersDelegate
    val path = PathArgs()
    val swgFile: String? get() = path.swgFile
}
