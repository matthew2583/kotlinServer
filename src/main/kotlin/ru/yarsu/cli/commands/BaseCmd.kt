package ru.yarsu.cli.commands

import com.beust.jcommander.ParametersDelegate
import ru.yarsu.cli.args.PathArgs

open class BaseCmd {
    @ParametersDelegate
    val path = PathArgs()
}
