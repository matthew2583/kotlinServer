package ru.yarsu.cli.args

import com.beust.jcommander.ParameterException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class PathArgsTest {
    private val validator = PathValidator()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun existingReadableFileIsAccepted() {
        val file = tempDir.resolve("test.csv").toFile()
        file.writeText("header\ndata")
        assertDoesNotThrow { validator.validate("--swg-file", file.absolutePath) }
    }

    @Test
    fun fileWithContentIsAccepted() {
        val file = tempDir.resolve("content.csv").toFile()
        file.writeText("Id,Name\n1,Test")
        assertDoesNotThrow { validator.validate("--dump-trucks-file", file.absolutePath) }
    }

    @Test
    fun nullPathIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--swg-file", null)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun emptyPathIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--swg-file", "")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun whitespacePathIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--swg-file", "   ")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun nonExistentFileIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--swg-file", "/non/existent/path/file.csv")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не найден"))
    }

    @Test
    fun directoryInsteadOfFileIsRejected() {
        val dir = tempDir.resolve("subdir").toFile()
        dir.mkdir()

        val exception =
            assertThrows<ParameterException> {
                validator.validate("--swg-file", dir.absolutePath)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не является файлом"))
    }

    @Test
    fun emptyFileIsRejected() {
        val emptyFile = tempDir.resolve("empty.csv").toFile()
        emptyFile.createNewFile()

        val exception =
            assertThrows<ParameterException> {
                validator.validate("--swg-file", emptyFile.absolutePath)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("пуст"))
    }

    @Test
    fun pathArgsHasNullValuesByDefault() {
        val pathArgs = PathArgs()
        assertNull(pathArgs.swgFile)
        assertNull(pathArgs.dumpTrucksFile)
        assertNull(pathArgs.employeesFile)
    }
}
